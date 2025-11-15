/**
 * 현재 유저가 팔로우하는 친구 목록 조회 및 렌더링 (토큰만 사용)
 */
async function loadFollowingList() {
    const followingList = document.getElementById('followingList');
    const token = localStorage.getItem('token');

    if (!token) {
        followingList.innerHTML = '<div class="text-danger text-center py-3">로그인 정보가 필요합니다.</div>';
        return;
    }

    // 🚩 데이터가 이미 로드된 경우 (탭 전환이 아닌 초기 로드 등), API 호출 없이 바로 렌더링
    if (appState.followingListData !== null) {
        renderFollowingList();
        return;
    }

    followingList.innerHTML = '<div class="text-center py-3">친구 목록 로드 중...</div>';

    try {
        const url = `${appState.BASE_URL}/following`;
        const users = await apiCall(url);

        appState.followingListData = users || [];

        appState.unreadMessageCounts = {}; // 초기화
        users.forEach(user => {
            if (user.unreadMessageCount > 0) {
                // (서버에서 가져온 초기 안 읽음 카운트 설정)
                appState.unreadMessageCounts[user.userUuid] = user.unreadMessageCount;
            }
        });

        renderFollowingList();

    } catch (e) {
        followingList.innerHTML = `<div class="text-danger text-center py-3">친구 목록 로드 중 오류 발생: ${e.message}</div>`;
    }
}


/**
 * followingListData를 사용하여 목록을 렌더링하고 이벤트 리스너를 재설정하는 헬퍼 함수
 */
function renderFollowingList() {
    const followingList = document.getElementById('followingList');

    if (!appState.followingListData || appState.followingListData.length === 0) {
        followingList.innerHTML = '<div class="text-muted text-center py-3">팔로우하는 친구가 없습니다.</div>';
        return;
    }

    followingList.innerHTML = appState.followingListData.map(user => renderFollowingItem(user)).join('');

    // 버튼 이벤트 리스너 재추가
    appState.followingListData.forEach(user => {
        // 언팔로우 버튼
        document.getElementById(`unfollowBtn-${user.userUuid}`).addEventListener('click', () => {
            handleUnfollowFromList(user.userUuid);
        });
        // 메시지 버튼
        document.getElementById(`messageBtn-${user.userUuid}`).addEventListener('click', () => {
            openChatModal(user.nickname, user.userUuid);
        });
        if (appState.unreadMessageCounts[user.userUuid] > 0) {
            updateFriendMessageBadge(user.userUuid, appState.unreadMessageCounts[user.userUuid]);
        }
    });
}

/**
 * 친구 목록에서 언팔로우 처리 및 UI 업데이트
 */
async function handleUnfollowFromList(followeeUuid) {
    const user = appState.followingListData.find(u => u.userUuid === followeeUuid);
    const nickname = user ? user.nickname : '사용자';

    if (!confirm(`${nickname} 님을 언팔로우하시겠습니까?`)) {
        return;
    }

    try {
        await apiCall(`${appState.BASE_URL}/follow/${followeeUuid}`, 'DELETE');
        alert(`${nickname} 님을 언팔로우했습니다.`);

        // 데이터 모델에서 제거 및 UI 업데이트
        appState.followingListData = appState.followingListData.filter(u => u.userUuid !== followeeUuid);

        // UI를 다시 렌더링
        renderFollowingList();
    } catch (e) {
        // 오류 처리
    }
}

/**
 * 초기 요청 목록 로드 및 카운트 반영 (DOM 로드 직후 실행)
 */
async function loadInitialRequests() {
    const requestCount = document.getElementById('requestCount');
    requestCount.textContent = '...';

    try {
        const users = await apiCall(`${appState.BASE_URL}/requests/received`);
        appState.receivedRequestsData = users || [];

        const count = appState.receivedRequestsData.length;
        document.getElementById('requestCount').textContent = count.toString();

        updateNotificationBadge();
    } catch (e) {
        console.error('❌ 초기 요청 목록 로드 실패:', e);
        requestCount.textContent = 'Err';
    }
}

/**
 * 8. 전체 사용자 검색 및 결과 렌더링 (토큰만 사용)
 */
async function searchUsers(nickname) {
    const searchResultList = document.getElementById('searchResultList');
    searchResultList.innerHTML = '<div class="text-center py-3">검색 중...</div>';

    try {
        const url = `${appState.BASE_URL}/search?nickname=${encodeURIComponent(nickname)}`;
        const users = await apiCall(url);

        if (!users || users.length === 0) {
            searchResultList.innerHTML = '<div class="text-muted text-center py-3">검색 결과가 없습니다.</div>';
            return;
        }

        searchResultList.innerHTML = users.map(user => renderUserSearchItem(user)).join('');

        // 검색 결과에 동적으로 생성된 팔로우/언팔로우 버튼에 이벤트 리스너 추가
        users.forEach(user => {
            const button = document.getElementById(`followBtn-${user.userUuid}`);
            if (button) {
                button.addEventListener('click', (event) => {
                    const action = event.target.dataset.action;
                    if (action === 'follow' || action === 'request') {
                        followUser(user.userUuid, event.target);
                    } else if (action === 'unfollow' || action === 'cancel') {
                        unfollowUser(user.userUuid, event.target);
                    }
                });
            }
        });

    } catch (e) {
        searchResultList.innerHTML = `<div class="text-danger text-center py-3">사용자 검색 중 오류 발생: ${e.message}</div>`;
    }
}

/**
 * 1. 팔로우하기 (토큰만 사용)
 */
async function followUser(followeeUuid, button) {
    try {
        await apiCall(`${appState.BASE_URL}/follow/${followeeUuid}`, 'POST');
        alert('팔로우 요청을 보냈습니다.');

        // 버튼 상태 업데이트: FOLLOW/NOT_FOLLOWING -> REQUESTED (요청 취소)
        button.textContent = '요청 취소';
        button.dataset.action = 'cancel'; // 요청 취소 액션
        button.classList.remove('btn-primary', 'btn-success');
        button.classList.add('btn-secondary'); // 회색으로 변경
    } catch (e) {
        // 오류 처리
    }
}

/**
 * 2. 언팔로우 / 팔로우 요청 취소 (토큰만 사용)
 * (검색 탭에서만 사용)
 */
async function unfollowUser(followeeUuid, button) {
    try {
        await apiCall(`${appState.BASE_URL}/follow/${followeeUuid}`, 'DELETE');
        alert('팔로우/요청을 취소했습니다.');

        // 버튼 상태 업데이트: UNFOLLOW/CANCEL -> NOT_FOLLOWING (팔로우)
        button.textContent = '팔로우';
        button.dataset.action = 'follow'; // 팔로우 액션
        button.classList.remove('btn-secondary', 'btn-success');
        button.classList.add('btn-primary'); // 파란색으로 변경
    } catch (e) {
        // 오류 처리
    }
}

/**
 * 11. 받은 팔로우 요청 목록 렌더링 (미리 로드된 데이터 사용)
 */
async function renderReceivedFollowRequests() {
    const receivedRequestList = document.getElementById('receivedRequestList');

    if (appState.receivedRequestsData === null) {
        receivedRequestList.innerHTML = '<div class="text-center py-3">요청 목록 로드 중...</div>';
    }

    if (!appState.receivedRequestsData || appState.receivedRequestsData.length === 0) {
        receivedRequestList.innerHTML = '<div class="text-muted text-center py-3">새로운 요청이 없습니다.</div>';
        return;
    }

    // 미리 로드된 데이터 사용
    receivedRequestList.innerHTML = appState.receivedRequestsData.map(user => renderUserRequestItem(user)).join('');

    // 요청 목록에 동적으로 생성된 수락/거절 버튼에 이벤트 리스너 추가
    appState.receivedRequestsData.forEach(user => {
        document.getElementById(`acceptBtn-${user.userUuid}`).addEventListener('click', () => acceptFollowRequest(user.userUuid));
        document.getElementById(`declineBtn-${user.userUuid}`).addEventListener('click', () => declineFollowRequest(user.userUuid));
    });
}

/**
 * 3. 팔로우 요청 수락 (토큰만 사용)
 * @param {string} requesterUuid 요청자 UUID
 */
async function acceptFollowRequest(requesterUuid) {
    const user = appState.receivedRequestsData.find(u => u.userUuid === requesterUuid);
    const nickname = user ? user.nickname : '알 수 없는 사용자';

    try {
        await apiCall(`${appState.BASE_URL}/accept/${requesterUuid}`, 'POST');
        alert(`${nickname} 님의 팔로우 요청을 수락했습니다.`);

        // 데이터 모델에서 제거 및 UI 업데이트
        appState.receivedRequestsData = appState.receivedRequestsData.filter(u => u.userUuid !== requesterUuid);

        // UI를 다시 렌더링하고 카운트 업데이트
        document.getElementById(`requestCount`).textContent = appState.receivedRequestsData.length.toString();
        renderReceivedFollowRequests();

        // 전역 알림 배지 카운트도 1 감소
        updateNotificationBadge();
    } catch (e) {
        // 오류 처리
    }
}

/**
 * 4. 팔로우 요청 거절 (토큰만 사용)
 * @param {string} requesterUuid 요청자 UUID
 */
async function declineFollowRequest(requesterUuid) {
    const user = appState.receivedRequestsData.find(u => u.userUuid === requesterUuid);
    const nickname = user ? user.nickname : '알 수 없는 사용자';

    try {
        await apiCall(`${appState.BASE_URL}/decline/${requesterUuid}`, 'POST');
        alert(`${nickname} 님의 팔로우 요청을 거절했습니다.`);

        // 데이터 모델에서 제거 및 UI 업데이트
        appState.receivedRequestsData = appState.receivedRequestsData.filter(u => u.userUuid !== requesterUuid);

        // UI를 다시 렌더링하고 카운트 업데이트
        document.getElementById(`requestCount`).textContent = appState.receivedRequestsData.length.toString();
        renderReceivedFollowRequests();

        // 전역 알림 배지 카운트도 1 감소
        updateNotificationBadge();
    } catch (e) {
        // 오류 처리
    }
}
