// 모든 전역 변수와 DOM 요소를
// 'appState'라는 객체 하나에 묶어서 관리합니다.
const appState = {
    // DOM 요소
    postGrid: document.getElementById('postGrid'),
    searchInput: document.getElementById('searchInput'),
    searchButton: document.getElementById('searchButton'),
    observerTarget: document.getElementById('observerTarget'),

    // 게시글 상태
    page: 0,
    loading: false,
    finished: false,
    currentKeyword: '',

    // API URL
    BASE_URL: '/api/users',

    // 사용자/알림 상태 (데이터)
    myUserUuid: null,
    notificationCount: 0,
    receivedRequestsData: null,
    followingListData: null,
    receivedNotifications: [],
    unreadMessageCounts: {},
    unreadInfoCount: 0,

    // 웹소켓/모달 인스턴스 (객체)
    globalStompClient: null,
    currentChatRoomId: null,
    currentChatTargetUuid: null,
    currentChatSubscription: null,
    chatModalInstance: null,
};

// 프로필 이미지 설정 (초기 실행)
document.addEventListener("DOMContentLoaded", () => {
    const token = localStorage.getItem("token");
    const searchNicknameBtn = document.getElementById('searchNicknameBtn');
    const nicknameSearchInput = document.getElementById('nicknameSearchInput');
    const searchResultList = document.getElementById('searchResultList');
    const requestTab = document.getElementById('request-tab');
    const followingTab = document.getElementById('following-tab');
    const notificationTab = document.getElementById('notification-tab');
    const refreshFollowingListBtn = document.getElementById('refreshFollowingListBtn');

    const friendshipModal = document.getElementById('friendshipModal');
    if (friendshipModal) {
        // 모달이 '보여지기 시작할 때' (맨 처음 1회만)
        friendshipModal.addEventListener('show.bs.modal', () => {
            // "친구 목록" 탭이 활성화 상태인지 확인
            if (followingTab.classList.contains('active')) {
                // (데이터가 없으면) API를 호출하여 친구 목록을 로드
                loadFollowingList();
            }
        });
    }

    const chatModalElement = document.getElementById('chatModal');
    if (chatModalElement) {
        appState.chatModalInstance = new bootstrap.Modal(chatModalElement);

        // 모달이 완전히 열린 뒤 스크롤을 맨 아래로 이동
        chatModalElement.addEventListener('shown.bs.modal', () => {
            const chatContent = document.getElementById('chatContent');
            chatContent.scrollTop = chatContent.scrollHeight;
        });

        // 채팅 모달이 닫힐 때 WebSocket 연결 해제
        chatModalElement.addEventListener('hidden.bs.modal', () => {
            unsubscribeFromChatRoom();
        });
    }

    // '알림' 탭의 목록 영역(notificationList)에 이벤트 리스너 추가
    const notificationListEl = document.getElementById('notificationList');
    if (notificationListEl) {
        notificationListEl.addEventListener('click', handleNotificationClick);
    }

    if (!token) {
        // 토큰이 없으면 요청 카운트 표시 로직도 실행하지 않음
        document.getElementById('requestCount').textContent = '0';
        return;
    }

    // 초기 요청 목록 로드
    loadInitialRequests();

    // 기타 알림 카운트 로드 (DB 조회)
    loadUnreadInfoCount();

    fetch("/users/info", {
        headers: {
            "Authorization": `Bearer ${token}`
        }
    })
        .then(res => {
            if (!res.ok) throw new Error("회원 정보 조회 실패");
            return res.json();
        })
        .then(user => {
            appState.myUserUuid = user.userUuid;
            const img = document.getElementById("profileImage");
            if (user.profileImagePath) {
                img.src = user.profileImagePath;
            }
            connectGlobalWebSocket(token);
        })
        .catch(err => console.error("❌ 프로필 이미지 로딩 실패:", err));

    // 친구 검색 버튼 이벤트
    searchNicknameBtn.addEventListener('click', () => {
        const nickname = nicknameSearchInput.value.trim();
        if (nickname) {
            searchUsers(nickname);
        } else {
            searchResultList.innerHTML = '<div class="text-muted text-center py-3">검색할 닉네임을 입력하세요.</div>';
        }
    });

    // 받은 요청 탭 클릭 시 목록 로드
    requestTab.addEventListener('shown.bs.tab', renderReceivedFollowRequests);

    // 친구 목록 탭 클릭 시 목록 로드
    followingTab.addEventListener('shown.bs.tab', loadFollowingList);

    /**
     * "친구 목록" 탭 내부의 새로고침 버튼 클릭 시
     */
    if (refreshFollowingListBtn) {
        refreshFollowingListBtn.addEventListener('click', () => {
            console.log('친구 목록 새로고침 중...');

            // 1. 캐시된 데이터를 null로 만들어,
            //    loadFollowingList 함수가 강제로 API를 호출하도록 함
            appState.followingListData = null;

            // 2. 친구 목록 로드 함수 재호출
            loadFollowingList();
        });
    }

    const sendChatBtn = document.getElementById('sendChatBtn');
    const chatInput = document.getElementById('chatInput');

    sendChatBtn.addEventListener('click', sendChatMessage); // 🚩 수정
    chatInput.addEventListener('keydown', (e) => {
        if (e.key === 'Enter') {
            e.preventDefault();
            sendChatMessage();
        }
    });

    // 알림 탭 클릭 시 이벤트
    notificationTab.addEventListener('shown.bs.tab', async () => {
        const listElement = document.getElementById('notificationList');
        listElement.innerHTML = '<div class="text-center py-3">알림 목록 로드 중...</div>';

        try {
            // 1. [신규] DB에서 알림 목록 API 호출 (페이징 0번째)
            const notificationSlice = await apiCall(`${appState.BASE_URL.replace('/users', '/notifications')}?page=0&size=15`);

            // 2. 전역 배열을 DB 데이터로 덮어쓰기
            appState.receivedNotifications = notificationSlice.content || [];

            // 3. 렌더링
            renderNotificationList(); // (다음 단계에서 이 함수도 수정)

            // 4. "모두 읽음" API 호출 (서버 DB 업데이트)
            await apiCall(`${appState.BASE_URL.replace('/users', '/notifications')}/read-all`, 'POST');

            // 5. (C) 기타 알림 카운트를 0으로 리셋
            appState.unreadInfoCount = 0;

            // 6. 배지 총합을 다시 계산 (A+B+C)
            updateNotificationBadge();

        } catch (e) {
            listElement.innerHTML = '<div class="text-danger text-center py-3">알림 로드에 실패했습니다.</div>';
            }
        });
    });

    // 검색 이벤트
    appState.searchButton.addEventListener('click', handleSearch);
    appState.searchInput.addEventListener('keydown', (e) => {
    if (e.key === 'Enter') {
        handleSearch();
    }

    observer.observe(appState.observerTarget); // 감시 시작
});


// 페이지 진입 시 첫 로딩
handleSearch();