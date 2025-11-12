const postGrid = document.getElementById('postGrid');
const searchInput = document.getElementById('searchInput');
const searchButton = document.getElementById('searchButton');
let page = 0;
let loading = false;
let finished = false;
const BASE_URL = '/api/users';

// 채팅 관련 전역 변수
let myUserUuid = null; // 현재 로그인한 사용자 UUID
let stompClient = null; // Stomp WebSocket 클라이언트
let currentChatRoomId = null; // 현재 열려있는 채팅방 ID
let currentChatTargetUuid = null; // 현재 대화 상대 UUID
let currentChatSubscription = null; // 현재 채팅방 구독

// 현재 검색어 기억
let currentKeyword = '';

// 감시 대상
const observerTarget = document.getElementById('observerTarget');

let receivedRequestsData = null;
let followingListData = null;

let chatModalInstance = null;

// 프로필 이미지 설정 (초기 실행)
document.addEventListener("DOMContentLoaded", () => {
    const token = localStorage.getItem("token");
    const searchNicknameBtn = document.getElementById('searchNicknameBtn');
    const nicknameSearchInput = document.getElementById('nicknameSearchInput');
    const searchResultList = document.getElementById('searchResultList');
    const requestTab = document.getElementById('request-tab');
    const followingTab = document.getElementById('following-tab');

    const chatModalElement = document.getElementById('chatModal');
    if (chatModalElement) {
        chatModalInstance = new bootstrap.Modal(chatModalElement);

        // 모달이 완전히 열린 뒤 스크롤을 맨 아래로 이동
        chatModalElement.addEventListener('shown.bs.modal', () => {
            const chatContent = document.getElementById('chatContent');
            chatContent.scrollTop = chatContent.scrollHeight;
        });

        // 채팅 모달이 닫힐 때 WebSocket 연결 해제
        chatModalElement.addEventListener('hidden.bs.modal', () => {
            disconnectWebSocket();
        });
    }

    if (!token) {
        // 토큰이 없으면 요청 카운트 표시 로직도 실행하지 않음
        document.getElementById('requestCount').textContent = '0';
        return;
    }

    // 초기 요청 목록 로드
    loadInitialRequests();

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
            myUserUuid = user.userUuid;
            const img = document.getElementById("profileImage");
            if (user.profileImagePath) {
                img.src = user.profileImagePath;
            }
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

    loadFollowingList();

    const sendChatBtn = document.getElementById('sendChatBtn');
    const chatInput = document.getElementById('chatInput');

    sendChatBtn.addEventListener('click', sendChatMessage); // 🚩 수정
    chatInput.addEventListener('keydown', (e) => {
        if (e.key === 'Enter') {
            e.preventDefault();
            sendChatMessage();
        }
    });
});

// 검색 이벤트
searchButton.addEventListener('click', handleSearch);
searchInput.addEventListener('keydown', (e) => {
    if (e.key === 'Enter') {
        handleSearch();
    }
});


function handleSearch() {
    page = 0;
    finished = false;
    postGrid.innerHTML = '';
    currentKeyword = searchInput.value.trim();
    fetchPosts();
}

// IntersectionObserver 설정
const observer = new IntersectionObserver((entries) => {
    if (entries[0].isIntersecting && !loading && !finished) {
        fetchPosts();
    }
}, {
    root: null,
    threshold: 0.1
});
observer.observe(observerTarget); // 감시 시작

// 게시글 불러오기
function fetchPosts() {
    loading = true;

    const params = new URLSearchParams({
        page,
        size: 6,
        title: currentKeyword
    });

    fetch(`/posts?${params.toString()}`)
        .then(res => {
            if (!res.ok) throw new Error('게시글 로딩 실패');
            return res.json();
        })
        .then(data => {
            const posts = data.content;

            if (posts.length === 0) {
                finished = true;
                return;
            }

            posts.forEach(renderPostCard);
            page++;
        })
        .catch(err => console.error('❌ 게시글 불러오기 실패:', err))
        .finally(() => loading = false);
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

    postGrid.appendChild(card);
}


// 🚩 [수정] apiCall 함수 전체를 이 코드로 교체하세요.

async function apiCall(endpoint, method = 'GET', data = null) {
    const token = localStorage.getItem('token');

    const headers = {
        'Content-Type': 'application/json',
    };

    // 토큰이 존재하면 Authorization 헤더를 추가합니다.
    if (token) {
        headers['Authorization'] = `Bearer ${token}`;
    }

    const config = {
        method: method,
        headers: headers,
    };

    if (data && (method === 'POST' || method === 'PUT')) {
        config.body = JSON.stringify(data);
    }

    try {
        const response = await fetch(endpoint, config);

        if (response.ok) {
            // 🚩 [수정] 204 No Content (내용 없음) 응답인지 먼저 확인
            if (response.status === 204) {
                return null; // 204 응답은 내용이 없으므로 null 반환
            }

            // 🚩 [수정] 200 OK 등 다른 성공 응답(POST 포함)은 JSON을 파싱하여 반환
            try {
                return await response.json();
            } catch (e) {
                // JSON 파싱에 실패하면 (응답 본문이 비어있는 200 OK 등) null 반환
                return null;
            }

        } else {
            const errorText = await response.text();
            throw new Error(errorText || `API 호출 실패: ${response.status}`);
        }
    } catch (error) {
        console.error('API 통신 오류:', error.message);
        // alert('오류 발생: ' + error.message); // 오류를 상위로 던지므로 alert 주석 처리
        throw error;
    }
}


// --- 3. 친구 기능 로직 구현 ---

/**
 * 현재 유저가 팔로우하는 친구 목록 조회 및 렌더링 (토큰만 사용)
 */
// ...
async function loadFollowingList() {
    const followingList = document.getElementById('followingList');
    const token = localStorage.getItem('token');

    if (!token) {
        followingList.innerHTML = '<div class="text-danger text-center py-3">로그인 정보가 필요합니다.</div>';
        return;
    }

    // 🚩 [수정 포인트 1] 데이터가 이미 로드된 경우 (탭 전환이 아닌 초기 로드 등), API 호출 없이 바로 렌더링
    if (followingListData !== null) {
        renderFollowingList();
        return;
    }

    followingList.innerHTML = '<div class="text-center py-3">친구 목록 로드 중...</div>';

    try {
        const url = `${BASE_URL}/following`;
        const users = await apiCall(url);

        followingListData = users || [];

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

    if (!followingListData || followingListData.length === 0) {
        followingList.innerHTML = '<div class="text-muted text-center py-3">팔로우하는 친구가 없습니다.</div>';
        return;
    }

    followingList.innerHTML = followingListData.map(user => renderFollowingItem(user)).join('');

    // 버튼 이벤트 리스너 재추가
    followingListData.forEach(user => {
        // 언팔로우 버튼
        document.getElementById(`unfollowBtn-${user.userUuid}`).addEventListener('click', () => {
            handleUnfollowFromList(user.userUuid);
        });
        // 메시지 버튼
        document.getElementById(`messageBtn-${user.userUuid}`).addEventListener('click', () => {
            openChatModal(user.nickname, user.userUuid);
        });
    });
}


/**
 * 친구 목록에서 언팔로우 처리 및 UI 업데이트
 */
async function handleUnfollowFromList(followeeUuid) {
    const user = followingListData.find(u => u.userUuid === followeeUuid);
    const nickname = user ? user.nickname : '사용자';

    if (!confirm(`${nickname} 님을 언팔로우하시겠습니까?`)) {
        return;
    }

    try {
        await apiCall(`${BASE_URL}/follow/${followeeUuid}`, 'DELETE');
        alert(`${nickname} 님을 언팔로우했습니다.`);

        // 데이터 모델에서 제거 및 UI 업데이트
        followingListData = followingListData.filter(u => u.userUuid !== followeeUuid);

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
        const users = await apiCall(`${BASE_URL}/requests/received`);
        receivedRequestsData = users || [];

        requestCount.textContent = receivedRequestsData.length.toString();

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
        const url = `${BASE_URL}/search?nickname=${encodeURIComponent(nickname)}`;
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
        await apiCall(`${BASE_URL}/follow/${followeeUuid}`, 'POST');
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
        await apiCall(`${BASE_URL}/follow/${followeeUuid}`, 'DELETE');
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

    if (receivedRequestsData === null) {
        receivedRequestList.innerHTML = '<div class="text-center py-3">요청 목록 로드 중...</div>';
    }

    if (!receivedRequestsData || receivedRequestsData.length === 0) {
        receivedRequestList.innerHTML = '<div class="text-muted text-center py-3">새로운 요청이 없습니다.</div>';
        return;
    }

    // 미리 로드된 데이터 사용
    receivedRequestList.innerHTML = receivedRequestsData.map(user => renderUserRequestItem(user)).join('');

    // 요청 목록에 동적으로 생성된 수락/거절 버튼에 이벤트 리스너 추가
    receivedRequestsData.forEach(user => {
        document.getElementById(`acceptBtn-${user.userUuid}`).addEventListener('click', () => acceptFollowRequest(user.userUuid));
        document.getElementById(`declineBtn-${user.userUuid}`).addEventListener('click', () => declineFollowRequest(user.userUuid));
    });
}


/**
 * 3. 팔로우 요청 수락 (토큰만 사용)
 * @param {string} requesterUuid 요청자 UUID
 */
async function acceptFollowRequest(requesterUuid) {
    const user = receivedRequestsData.find(u => u.userUuid === requesterUuid);
    const nickname = user ? user.nickname : '알 수 없는 사용자';

    try {
        await apiCall(`${BASE_URL}/accept/${requesterUuid}`, 'POST');
        alert(`${nickname} 님의 팔로우 요청을 수락했습니다.`);

        // 데이터 모델에서 제거 및 UI 업데이트
        receivedRequestsData = receivedRequestsData.filter(u => u.userUuid !== requesterUuid);

        // UI를 다시 렌더링하고 카운트 업데이트
        document.getElementById(`requestCount`).textContent = receivedRequestsData.length.toString();
        renderReceivedFollowRequests();

    } catch (e) {
        // 오류 처리
    }
}

/**
 * 4. 팔로우 요청 거절 (토큰만 사용)
 * @param {string} requesterUuid 요청자 UUID
 */
async function declineFollowRequest(requesterUuid) {
    const user = receivedRequestsData.find(u => u.userUuid === requesterUuid);
    const nickname = user ? user.nickname : '알 수 없는 사용자';

    try {
        await apiCall(`${BASE_URL}/decline/${requesterUuid}`, 'POST');
        alert(`${nickname} 님의 팔로우 요청을 거절했습니다.`);

        // 데이터 모델에서 제거 및 UI 업데이트
        receivedRequestsData = receivedRequestsData.filter(u => u.userUuid !== requesterUuid);

        // UI를 다시 렌더링하고 카운트 업데이트
        document.getElementById(`requestCount`).textContent = receivedRequestsData.length.toString();
        renderReceivedFollowRequests();
    } catch (e) {
        // 오류 처리
    }
}


// --- 4. HTML 템플릿 렌더링 함수 ---

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
                <!-- 메시지 버튼 -->
                <button 
                    class="btn btn-sm btn-info text-white me-2" 
                    id="messageBtn-${user.userUuid}"
                    data-uuid="${user.userUuid}"
                >
                    메시지
                </button>
                <!-- 언팔로우 버튼 -->
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
    if (chatModalInstance) {
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

        chatModalInstance.show();

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
            currentChatRoomId = roomResponse.roomId;

            // 5. [API] 메시지 로드 및 WebSocket 연결
            await loadAndConnectChat(currentChatRoomId);

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
        connectWebSocket(roomId);

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
/**
 * [WebSocket] Stomp 클라이언트 연결 (최신 라이브러리 방식)
 */
function connectWebSocket(roomId) {
    // 0. 이전 연결이 있다면 종료
    if (stompClient) {
        disconnectWebSocket();
    }

    // 1. 인증 토큰 준비
    const token = localStorage.getItem('token');

    // 2. StompJs.Client 인스턴스 생성
    // 🚩 [수정] Stomp.over() 대신 new StompJs.Client() 사용
    stompClient = new window.StompJs.Client({

        // 🚩 [수정] SockJS를 webSocketFactory를 통해 주입
        webSocketFactory: () => {
            // 🚩 [수정] Config와 일치하는 '/ws-stomp' 경로 및 window.SockJS 사용
            return new window.SockJS('/ws-stomp');
        },

        // 🚩 [수정] 연결 헤더 설정
        connectHeaders: {
            'Authorization': `Bearer ${token}`
        },

        // (디버깅 로그 비활성화)
        debug: () => {},

        // 🚩 [수정] 연결 성공 시 콜백
        onConnect: (frame) => {
            console.log('✅ STOMP 연결 성공:', frame);

            // 4. 특정 채팅방 구독 (/topic/room/{roomId})
            currentChatSubscription = stompClient.subscribe(`/topic/room/${roomId}`, (payload) => {
                // 구독 메시지 수신
                handleSubscriptionMessage(payload);
            });
        },

        // 🚩 [수정] 에러 발생 시 콜백
        onStompError: (frame) => {
            console.error('❌ STOMP 에러:', frame);
            document.getElementById('chatContent').innerHTML += `<div class="text-danger text-center p-1">실시간 연결에 실패했습니다.</div>`;
        }
    });

    // 3. Stomp 연결 활성화
    stompClient.activate();
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

    if (content && stompClient && stompClient.active && currentChatRoomId) {
        // 1. DTO 생성 (WebSocketMessageDto)
        const wsMessage = {
            roomId: currentChatRoomId,
            content: content
        };

        // 🚩 [수정] .send() -> .publish()
        stompClient.publish({
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
    if (messageId && stompClient && stompClient.active) {
        // 1. DTO 생성 (WebSocketReadMessageDto)
        const wsReadMessage = {
            messageId: messageId
        };

        // 🚩 [수정] .send() -> .publish()
        stompClient.publish({
            destination: "/app/chat.readMessage",
            body: JSON.stringify(wsReadMessage)
        });
    }
}

/**
 * [WebSocket] 연결 종료 (최신 라이브러리 방식)
 */
function disconnectWebSocket() {
    // 1. 구독 해제
    if (currentChatSubscription) {
        currentChatSubscription.unsubscribe();
        currentChatSubscription = null;
    }
    // 2. 연결 종료 (deactivate 사용)
    if (stompClient) {
        // 🚩 [수정] .disconnect() -> .deactivate()
        stompClient.deactivate(() => {
            console.log('⚪ STOMP 연결 해제됨');
        });
        stompClient = null;
    }
    // 3. 상태 초기화
    currentChatRoomId = null;
    currentChatTargetUuid = null;
    // 입력창 비활성화
    document.getElementById('chatInput').disabled = true;
    document.getElementById('sendChatBtn').disabled = true;
}

// --- 4. 채팅 UI 헬퍼 함수 ---

/**
 * [UI] 채팅창에 메시지 버블 표시
 */
function displayMessage(msgDto, isRealtime) {
    if (!myUserUuid) return; // 내 UUID가 없으면 비교 불가

    const chatContent = document.getElementById('chatContent');

    // 1. 메시지가 이미 있는지 확인 (중복 방지)
    if (document.querySelector(`.chat-bubble[data-message-id="${msgDto.messageId}"]`)) {
        return;
    }

    const isMine = (msgDto.senderUuid === myUserUuid);

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


    // 7. [중요] 실시간으로 받은 메시지(남의 메시지)면 즉시 읽음 처리
    if (!isMine && isRealtime) {
        sendReadReceipt(msgDto.messageId);
    }
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

// 페이지 진입 시 첫 로딩
handleSearch();