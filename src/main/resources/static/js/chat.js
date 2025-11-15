// 모달 드래그 기능 구현 함수
function dragElement(dialogElement, headerElement) {
    let pos1 = 0, pos2 = 0, pos3 = 0, pos4 = 0;

    if (headerElement) {
        // 모달 헤더를 드래그 핸들로 지정
        headerElement.onmousedown = dragMouseDown;
    } else {
        // 헤더가 없으면 모달 자체를 드래그 핸들로 지정
        dialogElement.onmousedown = dragMouseDown;
    }

    function dragMouseDown(e) {
        e = e || window.event;
        // 드래그가 시작되면 텍스트 선택 방지
        e.preventDefault();

        // 현재 마우스 위치를 가져와 초기 위치로 설정
        pos3 = e.clientX;
        pos4 = e.clientY;

        document.onmouseup = closeDragElement;
        document.onmousemove = elementDrag;
    }

    function elementDrag(e) {
        e = e || window.event;
        e.preventDefault();

        // 마우스의 이동 거리 계산
        pos1 = pos3 - e.clientX;
        pos2 = pos4 - e.clientY;
        pos3 = e.clientX;
        pos4 = e.clientY;

        // 새 위치 계산 (현재 위치 - 이동 거리)
        // offsetTop과 offsetLeft 대신 CSS top/left 속성을 사용합니다.
        const currentTop = dialogElement.offsetTop;
        const currentLeft = dialogElement.offsetLeft;

        // 새 위치 적용
        dialogElement.style.top = (currentTop - pos2) + "px";
        dialogElement.style.left = (currentLeft - pos1) + "px";
    }

    function closeDragElement() {
        // 마우스 버튼을 놓으면 모든 리스너를 제거합니다.
        document.onmouseup = null;
        document.onmousemove = null;
    }
}

async function openChatModal(nickname, targetUserUuid) {
    // (모달을 열기 전에 카운트를 0으로 만듦)
    if (appState.unreadMessageCounts[targetUserUuid] > 0) {
        appState.unreadMessageCounts[targetUserUuid] = 0;
        updateFriendMessageBadge(targetUserUuid, 0);

        updateNotificationBadge();
    }

    if (appState.chatModalInstance) {
        // 1. 모달 UI 준비 (로딩 스피너)
        document.getElementById('chatTargetNickname').textContent = nickname;
        currentChatTargetUuid = targetUserUuid;

        const chatContent = document.getElementById('chatContent');
        chatContent.innerHTML = `
            <div class="chat-loading">
                <span class="spinner-border spinner-border-sm"></span>
                <span class="ms-2">채팅방 정보를 불러오는 중...</span>
            </div>`;

        // 입력창 비활성화
        document.getElementById('chatInput').disabled = true;
        document.getElementById('sendChatBtn').disabled = true;

        appState.chatModalInstance.show();

        // 2. 친구 모달 닫기
        const friendshipModalEl = document.getElementById('friendshipModal');
        bootstrap.Modal.getInstance(friendshipModalEl)?.hide();

        // 3. 드래그 기능 활성화
        const dialog = document.querySelector('#chatModal .modal-dialog');
        const header = document.getElementById('chatModalHeader');
        if (window.innerWidth >= 768) {
            dragElement(dialog, header);
        }

        // 4. [API] 1:1 채팅방 생성 또는 조회
        try {
            const roomResponse = await findOrCreateChatRoom(targetUserUuid);
            appState.currentChatRoomId = roomResponse.roomId;

            // 5. [API] 메시지 로드 및 WebSocket 연결
            await loadAndConnectChat(appState.currentChatRoomId);

            // 6. 연결 완료 후 입력창 활성화
            document.getElementById('chatInput').disabled = false;
            document.getElementById('sendChatBtn').disabled = false;
            document.getElementById('chatInput').focus(); // 입력창 포커스

        } catch (error) {
            chatContent.innerHTML = `<div class="text-danger text-center p-3">채팅방 연결에 실패했습니다: ${error.message}</div>`;
        }
    }
}

/**
 * [API 1] 1:1 채팅방 생성 또는 조회 (POST /api/chats/create)
 */
async function findOrCreateChatRoom(targetUserUuid) {
    const requestBody = {
        targetUserUuids: [targetUserUuid],
        roomName: null // 1:1 채팅이므로 이름은 null
    };
    try {
        const response = await apiCall('/api/chats/create', 'POST', requestBody);
        return response; // { roomId, roomName }
    } catch (error) {
        console.error("❌ 채팅방 생성/조회 실패:", error);
        throw error;
    }
}

/**
 * [API 2 & 3] 지난 메시지 로드 및 WebSocket 연결
 */
async function loadAndConnectChat(roomId) {
    const chatContent = document.getElementById('chatContent');
    chatContent.innerHTML = `<div class="chat-loading">
                                <span class="spinner-border spinner-border-sm"></span>
                                <span class="ms-2">지난 메시지를 불러오는 중...</span>
                            </div>`;

    try {
        // 1. [API 2] 지난 메시지 로드 (GET /api/messages/{roomId})
        const messagePage = await apiCall(`/api/messages/${roomId}`);

        chatContent.innerHTML = ''; // 로딩 스피너 제거

        const messages = messagePage.content;
        messages.reverse(); // 시간 순 정렬 (오래된 -> 최신)

        messages.forEach(msgDto => {
            displayMessage(msgDto, false); // false: 실시간 아님 (읽음 처리 안 함)
        });

        // 렌더링이 완료된 후 스크롤을 맨 아래로 이동시키기 위해 setTimeout 사용
        setTimeout(() => {
            chatContent.scrollTop = chatContent.scrollHeight;
        }, 0); // 0밀리초 지연 = 다음 이벤트 루프에서 실행

        // 2. WebSocket 연결
        subscribeToChatRoom(roomId);

        // 3. [API 3] 채팅방 읽음 처리 (POST /api/chats/{roomId}/read)
        // (채팅방에 입장했음을 서버에 알림)
        await apiCall(`/api/chats/${roomId}/read`, 'POST');

    } catch (error) {
        console.error("❌ 메시지 로드/연결 실패:", error);
        chatContent.innerHTML = `<div class="text-danger text-center p-3">메시지 로드에 실패했습니다.</div>`;
    }
}

/**
 * [WebSocket] Stomp 클라이언트 연결
 */
function subscribeToChatRoom(roomId) {
    if (!appState.globalStompClient || !appState.globalStompClient.active) {
        console.error("STOMP 클라이언트가 연결되지 않았습니다.");
        return;
    }
    if (appState.currentChatSubscription) { // 중복 구독 방지
        appState.currentChatSubscription.unsubscribe();
    }
    // 전역 클라이언트를 사용하여 '구독'만 실행
    appState.currentChatSubscription = appState.globalStompClient.subscribe(`/topic/room/${roomId}`, (payload) => {
        handleSubscriptionMessage(payload);
    });
    console.log(`✅ [Chat] STOMP 채팅방 구독: /topic/room/${roomId}`);
}


/**
 * [WebSocket] 구독 메시지 처리 (새 메시지 OR 읽음 이벤트)
 */
function handleSubscriptionMessage(payload) {
    const receivedMessage = JSON.parse(payload.body);

    // 타입 구분
    if (receivedMessage.messageId && receivedMessage.content) {
        // 1. 새 메시지 (ChatMessageDto)
        displayMessage(receivedMessage, true); // true: 실시간 메시지

        // 실시간 메시지 수신 시, 여기서 직접 스크롤을 호출합니다.
        const chatContent = document.getElementById('chatContent');
        setTimeout(() => {
            chatContent.scrollTop = chatContent.scrollHeight;
        }, 0); // 렌더링 후 스크롤

    } else if (receivedMessage.messageId && receivedMessage.unreadCount !== undefined) {
        // 2. 읽음 이벤트 (MessageReadEventDto)
        updateUnreadCount(receivedMessage);
    }
}

/**
 * [WebSocket] 메시지 전송 (Send) (최신 라이브러리 방식)
 */
function sendChatMessage() {
    const chatInput = document.getElementById('chatInput');
    const content = chatInput.value.trim();

    if (content && appState.globalStompClient && appState.globalStompClient.active && appState.currentChatRoomId) {
        // 1. DTO 생성 (WebSocketMessageDto)
        const wsMessage = {
            roomId: appState.currentChatRoomId,
            content: content
        };

        appState.globalStompClient.publish({
            destination: "/app/chat.sendMessage",
            body: JSON.stringify(wsMessage)
            // (헤더는 connect 시 설정됨)
        });

        chatInput.value = ''; // 입력창 비우기
    }
}

/**
 * [WebSocket] 읽음 확인 메시지 전송 (Read) (최신 라이브러리 방식)
 */
function sendReadReceipt(messageId) {
    if (messageId && appState.globalStompClient && appState.globalStompClient.active) {
        // 1. DTO 생성 (WebSocketReadMessageDto)
        const wsReadMessage = {
            messageId: messageId
        };

        appState.globalStompClient.publish({
            destination: "/app/chat.readMessage",
            body: JSON.stringify(wsReadMessage)
        });
    }
}

/**
 * [WebSocket] 채팅방 구독 해지 (모달이 닫힐 때)
 */
function unsubscribeFromChatRoom() {
    // 1. 채팅방 구독 해지
    if (appState.currentChatSubscription) {
        appState.currentChatSubscription.unsubscribe();
        appState.currentChatSubscription = null;
        console.log('⚪ [Chat] STOMP 채팅방 구독 해제됨');
    }
    // (전역 연결은 절대 끊지 않음)

    // 2. [추가] 현재 채팅 상태 변수 초기화 (중요)
    appState.currentChatRoomId = null;
    appState.currentChatTargetUuid = null;

    // 3. [추가] 입력창 비활성화
    const chatInput = document.getElementById('chatInput');
    const sendChatBtn = document.getElementById('sendChatBtn');
    if(chatInput) chatInput.disabled = true;
    if(sendChatBtn) sendChatBtn.disabled = true;
}

/**
 * [UI] 실시간 읽음 이벤트 수신 시, 안 읽음 카운트 업데이트
 */
function updateUnreadCount(readEventDto) {
    // 1. 화면에서 해당 메시지 버블 찾기
    const messageBubble = document.querySelector(`.chat-bubble[data-message-id="${readEventDto.messageId}"]`);
    if (messageBubble) {
        // 2. 안 읽음 카운트 Span 찾기
        const unreadSpan = messageBubble.querySelector('.unread-count');
        if (unreadSpan) {
            // 3. 카운트 업데이트 (0이면 숨기기)
            unreadSpan.textContent = readEventDto.unreadCount > 0 ? readEventDto.unreadCount : '';
        }
    }
}
