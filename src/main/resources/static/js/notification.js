/**
 * 페이지 로드 시 1회 실행, 전역 웹소켓 연결
 * - 알림 토픽 구독
 */
function connectGlobalWebSocket(token) {
    // 이미 연결되었거나, UUID가 없으면 실행 중단
    if (appState.globalStompClient || !appState.myUserUuid) return;

    console.log('⏳ [Global] STOMP 연결 시도...');

    // 1. StompJs.Client 인스턴스 생성
    appState.globalStompClient = new window.StompJs.Client({
        webSocketFactory: () => new window.SockJS('/ws-stomp'), // SockJS 사용
        connectHeaders: {
            'Authorization': `Bearer ${token}`
        },
        debug: () => {}, // 디버그 로그 비활성화
        reconnectDelay: 5000, // [권장] 5초마다 자동 재연결 시도

        // 2. 연결 성공 시 콜백
        onConnect: (frame) => {
            console.log('✅ [Global] STOMP 연결 성공:', frame);

            // 3. [핵심] 알림 토픽 구독 (/topic/notifications/{myUserUuid})
            appState.globalStompClient.subscribe(`/topic/notifications/${appState.myUserUuid}`, handleNotification);
        },

        // 4. 에러 발생 시 콜백
        onStompError: (frame) => {
            console.error('❌ [Global] STOMP 에러:', frame);
        }
    });

    // 5. Stomp 연결 활성화
    appState.globalStompClient.activate();
}

/**
 * 알림 토픽 메시지 수신 핸들러
 */
function handleNotification(payload) {
    try {
        const notification = JSON.parse(payload.body);
        console.log('🔔 새 알림 수신!', notification);


        // 2. 알림 유형에 따라 '소스 (A)' 또는 '소스 (B)'를 업데이트
        switch(notification.type) {
            case 'NEW_FOLLOWER':
            case 'FOLLOW_ACCEPTED':
                // 기타 알림 카운트를 1 증가
                appState.unreadInfoCount++;
                break;
            case 'FOLLOW_REQUEST':
                // (소스 A가 변경될 것이므로 loadInitialRequests 호출)
                loadInitialRequests(); // 이 함수가 마지막에 updateNotificationBadge()를 호출함
                return; // loadInitialRequests가 배지를 업데이트하므로 여기서 종료
            case 'NEW_MESSAGE':
                if (notification.roomId === appState.currentChatRoomId) {
                    return; // 채팅방이 열려있으면 무시
                }

                // (소스 B 업데이트)
                const senderUuid = notification.senderUserUuid;
                if (!appState.unreadMessageCounts[senderUuid]) {
                    appState.unreadMessageCounts[senderUuid] = 0;
                }
                appState.unreadMessageCounts[senderUuid]++;
                updateFriendMessageBadge(senderUuid, appState.unreadMessageCounts[senderUuid]);
                break;
        }

        // 3. [수정] 모든 소스 변경이 끝난 후, 배지 총합을 다시 계산
        updateNotificationBadge(); // 인자 없이 호출

    } catch (e) {
        console.error("알림 메시지 파싱 실패", e);
    }
}

/**
 * [신규] (C) 기타 알림 (메시지, 팔로우 요청 제외)의 안 읽은 개수를 DB에서 조회
 */
async function loadUnreadInfoCount() {
    try {
        // 1. [신규] API 호출
        const count = await apiCall(`${appState.BASE_URL.replace('/users', '/notifications')}/unread-count`);

        // 2. 전역 변수에 저장
        appState.unreadInfoCount = count || 0;

        // 3. 배지 총합계 업데이트
        updateNotificationBadge();

    } catch (e) {
        console.error("❌ 안 읽은 '기타 알림' 개수 로드 실패:", e);
        // (실패해도 헤더 배지는 0으로 렌더링됨)
    }
}

/**
 * 렌더링된 알림 목록을 클릭했을 때 실행되는 핸들러
 */
function handleNotificationClick(event) {
    // 1. 클릭된 항목(또는 그 부모)이 .notification-item-clickable인지 확인
    const item = event.target.closest('.notification-item-clickable');
    if (!item) return; // 알림 항목이 아닌 곳을 클릭했으면 무시

    // 2. data 속성에서 알림 정보 읽기
    const type = item.dataset.notificationType;
    const senderUuid = item.dataset.senderUuid;
    const senderNickname = item.dataset.senderNickname;

    // 3. 알림 유형에 따라 분기
    switch (type) {
        case 'NEW_MESSAGE':
            if (!senderUuid || !senderNickname) {
                console.error("채팅방을 열기 위한 사용자 정보가 없습니다.");
                return;
            }
            // 1. (기존 함수 재사용) 채팅방 모달 열기
            openChatModal(senderNickname, senderUuid);
            // (openChatModal 함수가 자동으로 친구 모달을 닫아줄 것입니다.)
            break;

        case 'FOLLOW_REQUEST':
            // 2. "받은 요청" 탭을 수동으로 활성화
            const requestTabEl = document.getElementById('request-tab');
            if (requestTabEl) {
                new bootstrap.Tab(requestTabEl).show();
            }
            break;

        case 'NEW_FOLLOWER':
        case 'FOLLOW_ACCEPTED':
            // 3. "친구 목록" 탭을 수동으로 활성화
            const followingTabEl = document.getElementById('following-tab');
            if (followingTabEl) {
                new bootstrap.Tab(followingTabEl).show();
            }
            break;

        default:
            // (그 외 알림은 아무 동작 안 함)
            break;
    }
}

/**
 * [신규] 친구 목록의 '메시지' 버튼 배지 UI를 업데이트하는 함수
 * @param {string} friendUuid 친구의 UUID
 * @param {number} count 표시할 숫자
 */
function updateFriendMessageBadge(friendUuid, count) {
    const badge = document.getElementById(`messageBadge-${friendUuid}`);

    // 친구 목록 탭이 아직 열리지 않아 DOM에 요소가 없으면 그냥 종료
    if (!badge) return;

    if (count > 0) {
        badge.textContent = count;
        badge.classList.remove('d-none');
    } else {
        badge.textContent = '0';
        badge.classList.add('d-none');
    }
}

/**
 * 모든 알림 소스를 다시 계산하여 헤더/모달 배지를 업데이트합니다.
 */
function updateNotificationBadge() {
    // 1. 소스 (A): "받은 요청" 수
    const followRequestCount = appState.receivedRequestsData ? appState.receivedRequestsData.length : 0;

    // 2. 소스 (B): "총 안 읽은 메시지" 수
    // (이 변수는 '친구 목록' 탭의 배지를 위해 계산하지만, 헤더 총합에는 사용하지 않습니다.)
    let totalUnreadMessages = 0;
    for (const uuid in appState.unreadMessageCounts) {
        totalUnreadMessages += appState.unreadMessageCounts[uuid];
    }

    // 3. 소스 (C): "기타 알림" (팔로우 수락, 새 팔로워)
    const infoAlertCount = appState.unreadInfoCount;

    // 4. [수정] 총합 계산 (헤더 배지용)
    // 헤더 배지는 (A) 받은 요청 + (C) 기타 알림만 포함합니다.
    // (B) 총 안 읽은 메시지(totalUnreadMessages)는 여기서 제외합니다.
    appState.notificationCount = followRequestCount + infoAlertCount;
    console.log(followRequestCount);
    console.log(infoAlertCount);

    // --- UI 업데이트 ---
    const headerBadge = document.getElementById('notificationBadge');
    const modalBadge = document.getElementById('notificationModalBadge'); // '알림' 탭 배지

    // 5. 헤더 배지 업데이트 (총합 A+C)
    const totalCountText = appState.notificationCount.toString();
    const showTotal = appState.notificationCount > 0;
    if (headerBadge) {
        headerBadge.textContent = totalCountText;
        showTotal ? headerBadge.classList.remove('d-none') : headerBadge.classList.add('d-none');
    }

    // 6. '알림' 탭 배지 업데이트 (C만)
    // (이 로직은 기존과 동일하게 유지)
    const infoCountText = infoAlertCount.toString();
    const showInfo = infoAlertCount > 0;
    if (modalBadge) {
        modalBadge.textContent = infoCountText;
        showInfo ? modalBadge.classList.remove('d-none') : modalBadge.classList.add('d-none');
    }
}

