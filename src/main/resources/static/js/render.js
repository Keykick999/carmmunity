/**
 * '알림' 탭을 렌더링하는 함수
 */
function renderNotificationList() {
    const listElement = document.getElementById('notificationList');

    if (appState.receivedNotifications.length === 0) {
        listElement.innerHTML = '<div class="text-muted text-center py-3">표시할 알림이 없습니다.</div>';
        return;
    }

    // 배열의 내용을 HTML로 변환
    listElement.innerHTML = appState.receivedNotifications.map(notification => {
        // 알림 유형에 따라 아이콘 변경 (예시)
        let icon = '🔔';
        if (notification.type === 'NEW_FOLLOWER') icon = '❤️';
        if (notification.type === 'FOLLOW_REQUEST') icon = '✉️';
        if (notification.type === 'FOLLOW_ACCEPTED') icon = '🤝';

        return `
            <div class="list-group-item list-group-item-action d-flex align-items-center notification-item-clickable" 
                 style="cursor: pointer;"
                 data-notification-type="${notification.type}"
                 data-sender-uuid="${notification.senderUserUuid || ''}"
                 data-sender-nickname="${notification.senderNickname || ''}"
                 data-room-id="${notification.roomId || ''}">

                <span style="font-size: 1.2rem; margin-right: 12px;">${icon}</span>
                <div style="line-height: 1.3;">
                    <span class="fw-bold">${notification.senderNickname}</span>
                    <small class="text-muted d-block">${notification.message}</small>
                </div>
            </div>
        `;
    }).join('');
}


// 게시글 카드 렌더링
function renderPostCard(post) {
    const card = document.createElement('div');
    card.className = 'post-card';
    card.onclick = () => {
        location.href = `/postDetail?postId=${post.postId}`;
    };

    const thumbnailUrl = `/posts/image/${post.postId}`;

    card.innerHTML = `
        <img class="thumbnail" src="${thumbnailUrl}" alt="썸네일">
        <div class="post-content">
            <div class="post-title">${post.postTitle}</div>
            <div class="post-meta">
                <span>${post.nickname}</span>
                <span>${new Date(post.updatedAt).toLocaleString()}</span>
            </div>
        </div>
    `;

    appState.postGrid.appendChild(card);
}


/**
 * 검색 결과 항목 HTML 생성 (상태에 따라 버튼 변경)
 * @param {Object} user UserSummaryDto 객체 (반드시 relationshipStatus 포함)
 */
function renderUserSearchItem(user) {
    let buttonText = '팔로우';
    let buttonAction = 'follow';
    let buttonClass = 'btn-primary';
    let isDisabled = false;

    // 서버에서 반환한 relationshipStatus에 따라 버튼 상태 설정
    switch (user.relationshipStatus) {
        case 'FOLLOWING':
            buttonText = '언팔로우';
            buttonAction = 'unfollow';
            buttonClass = 'btn-success'; // 팔로우 중일 때는 초록색
            break;
        case 'REQUESTED':
            buttonText = '요청 취소';
            buttonAction = 'cancel';
            buttonClass = 'btn-secondary'; // 요청 중일 때는 회색
            break;
        case 'BLOCKED':
            buttonText = '차단됨';
            buttonAction = 'blocked';
            buttonClass = 'btn-danger';
            isDisabled = true;
            break;
        case 'NOT_FOLLOWING':
        default:
            buttonText = '팔로우';
            buttonAction = 'follow';
            buttonClass = 'btn-primary';
            break;
    }

    return `
        <div class="user-list-item list-group-item" id="searchItem-${user.userUuid}">
            <div class="user-info">
                <img src="${user.profileImagePath || '/assets/default-profile.png'}" alt="프로필">
                <span>${user.nickname}</span>
            </div>
            <button 
                class="btn btn-sm ${buttonClass}" 
                id="followBtn-${user.userUuid}"
                data-action="${buttonAction}"
                data-uuid="${user.userUuid}"
                ${isDisabled ? 'disabled' : ''}
            >
                ${buttonText}
            </button>
        </div>
    `;
}


/**
 * 친구 목록 항목 HTML 생성
 * @param {Object} user UserSummaryDto 객체 (친구 정보)
 */
function renderFollowingItem(user) {
    return `
        <div class="user-list-item list-group-item" id="followingItem-${user.userUuid}">
            <div class="user-info">
                <img src="${user.profileImagePath || '/assets/default-profile.png'}" alt="프로필">
                <span>${user.nickname}</span>
            </div>
            <div>
                <!-- [수정] 1. 버튼에 position-relative 추가 -->
                <button 
                    class="btn btn-sm btn-info text-white me-2 position-relative" 
                    id="messageBtn-${user.userUuid}"
                    data-uuid="${user.userUuid}"
                >
                    메시지
                    <!-- [수정] 2. 안 읽음 카운트 배지 span 추가 -->
                    <span id="messageBadge-${user.userUuid}"
                          class="position-absolute top-0 start-100 translate-middle badge rounded-pill bg-danger d-none"
                          style="font-size: 0.7em; padding: 4px 6px;">
                        0
                    </span>
                </button>
                <!-- 언팔로우 버튼 (기존과 동일) -->
                <button 
                    class="btn btn-sm btn-danger" 
                    id="unfollowBtn-${user.userUuid}"
                    data-uuid="${user.userUuid}"
                >
                    언팔로우
                </button>
            </div>
        </div>
    `;
}


/**
 * 받은 요청 목록 항목 HTML 생성
 * @param {Object} user UserSummaryDto 객체 (요청자 정보)
 */
function renderUserRequestItem(user) {
    return `
        <div class="user-list-item list-group-item" id="requestItem-${user.userUuid}">
            <div class="user-info">
                <img src="${user.profileImagePath || '/assets/default-profile.png'}" alt="프로필">
                <span>${user.nickname}</span>
            </div>
            <div>
                <button 
                    class="btn btn-sm btn-success me-2" 
                    id="acceptBtn-${user.userUuid}"
                    data-uuid="${user.userUuid}"
                >
                    수락
                </button>
                <button 
                    class="btn btn-sm btn-danger" 
                    id="declineBtn-${user.userUuid}"
                    data-uuid="${user.userUuid}"
                >
                    거절
                </button>
            </div>
        </div>
    `;
}


/**
 * [UI] 채팅창에 메시지 버블 표시
 */
function displayMessage(msgDto, isRealtime) {
    if (!appState.myUserUuid) return; // 내 UUID가 없으면 비교 불가

    const chatContent = document.getElementById('chatContent');

    // 1. 메시지가 이미 있는지 확인 (중복 방지)
    if (document.querySelector(`.chat-bubble[data-message-id="${msgDto.messageId}"]`)) {
        return;
    }

    const isMine = (msgDto.senderUuid === appState.myUserUuid);

    // 2. 말풍선(bubble) 생성
    const bubble = document.createElement('div');
    bubble.className = `chat-bubble ${isMine ? 'outgoing' : 'incoming'}`;
    bubble.dataset.messageId = msgDto.messageId; // 읽음 처리를 위해 ID 저장

    // 3. 메시지 내용 추가
    const content = document.createElement('span');
    content.textContent = msgDto.content;
    bubble.appendChild(content);

    // 4. 안 읽음 카운트 추가 (보낸 메시지인 경우)
    if (isMine) {
        const unreadSpan = document.createElement('span');
        unreadSpan.className = 'unread-count';
        if (msgDto.unreadCount > 0) {
            unreadSpan.textContent = msgDto.unreadCount;
        }
        bubble.appendChild(unreadSpan);
    }

    // 5. 채팅창에 추가
    chatContent.appendChild(bubble);


    // 7. 실시간으로 받은 메시지(남의 메시지)면 즉시 읽음 처리
    if (!isMine && isRealtime) {
        sendReadReceipt(msgDto.messageId);
    }
}