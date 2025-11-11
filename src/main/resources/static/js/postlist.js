const postGrid = document.getElementById('postGrid');
const searchInput = document.getElementById('searchInput');
const searchButton = document.getElementById('searchButton');
let page = 0;
let loading = false;
let finished = false;
const BASE_URL = '/api/users';

// 현재 검색어 기억
let currentKeyword = '';

// 감시 대상
const observerTarget = document.getElementById('observerTarget');

// 프로필 이미지 설정 (초기 실행)
document.addEventListener("DOMContentLoaded", () => {
    const token = localStorage.getItem("token");
    const searchNicknameBtn = document.getElementById('searchNicknameBtn');
    const nicknameSearchInput = document.getElementById('nicknameSearchInput');
    const searchResultList = document.getElementById('searchResultList');
    const requestTab = document.getElementById('request-tab');

    if (!token) return;

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
    requestTab.addEventListener('shown.bs.tab', loadReceivedFollowRequests);
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
            // 응답 본문이 없는 경우 (200 OK, 204 No Content 등)
            if (method !== 'GET' || response.status === 204) {
                return null;
            }
            return await response.json();
        } else {
            const errorText = await response.text();
            throw new Error(errorText || `API 호출 실패: ${response.status}`);
        }
    } catch (error) {
        console.error('API 통신 오류:', error.message);
        alert('오류 발생: ' + error.message);
        throw error;
    }
}

// --- 3. 친구 기능 로직 구현 ---

/**
 * 8. 전체 사용자 검색 및 결과 렌더링
 * @param {string} nickname 검색할 닉네임
 */
async function searchUsers(nickname) {
    const searchResultList = document.getElementById('searchResultList');
    searchResultList.innerHTML = '<div class="text-center py-3">검색 중...</div>';

    try {
        const url = `${BASE_URL}/search?nickname=${encodeURIComponent(nickname)}`;
        const users = await apiCall(url); // [UserSummaryDto, ...]

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
                        // 팔로우 요청과 그냥 팔로우는 동일 API (POST)를 사용
                        followUser(user.userUuid, event.target);
                    } else if (action === 'unfollow' || action === 'cancel') {
                        // 언팔로우와 요청 취소는 동일 API (DELETE)를 사용
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
 * 1. 팔로우하기
 * @param {string} followeeUuid 팔로우할 상대방 UUID
 * @param {HTMLElement} button 엘리먼트 (상태 변경용)
 */
async function followUser(followeeUuid, button) {
    try {
        await apiCall(`${BASE_URL}/follow/${followeeUuid}`, 'POST');
        alert('팔로우/요청을 보냈습니다.');

        // 팔로우 요청 API는 (1) 즉시 FOLLOWING이 되거나 (공개 계정) (2) PENDING이 됨 (비공개 계정)
        // 클라이언트에서는 서버 응답을 정확히 알 수 없으므로, 일단 요청 취소 상태로 가정하고
        // 실제 상태는 서버에서 다시 받아와야 정확하지만, UX를 위해 PENDING 상태로 임시 업데이트합니다.

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
 * 2. 언팔로우 / 팔로우 요청 취소
 * @param {string} followeeUuid 언팔로우할 상대방 UUID
 * @param {HTMLElement} button 엘리먼트 (상태 변경용)
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
 * 11. 받은 팔로우 요청 목록 조회 및 렌더링
 */
async function loadReceivedFollowRequests() {
    const receivedRequestList = document.getElementById('receivedRequestList');
    receivedRequestList.innerHTML = '<div class="text-center py-3">요청 목록 로드 중...</div>';

    try {
        const users = await apiCall(`${BASE_URL}/requests/received`); // [UserSummaryDto, ...]
        const requestCount = document.getElementById('requestCount');

        if (!users || users.length === 0) {
            receivedRequestList.innerHTML = '<div class="text-muted text-center py-3">새로운 요청이 없습니다.</div>';
            requestCount.textContent = '0';
            return;
        }

        receivedRequestList.innerHTML = users.map(user => renderUserRequestItem(user)).join('');
        requestCount.textContent = users.length.toString();

        // 요청 목록에 동적으로 생성된 수락/거절 버튼에 이벤트 리스너 추가
        users.forEach(user => {
            document.getElementById(`acceptBtn-${user.userUuid}`).addEventListener('click', () => acceptFollowRequest(user.userUuid));
            document.getElementById(`declineBtn-${user.userUuid}`).addEventListener('click', () => declineFollowRequest(user.userUuid));
        });

    } catch (e) {
        receivedRequestList.innerHTML = `<div class="text-danger text-center py-3">요청 목록 로드 중 오류 발생: ${e.message}</div>`;
        requestCount.textContent = 'Err';
    }
}

/**
 * 3. 팔로우 요청 수락
 * @param {string} requesterUuid 요청자 UUID
 */
async function acceptFollowRequest(requesterUuid) {
    try {
        await apiCall(`${BASE_URL}/accept/${requesterUuid}`, 'POST');
        alert(`${requesterUuid} 님의 팔로우 요청을 수락했습니다.`);

        // UI에서 해당 항목 제거 후 목록 새로고침
        document.getElementById(`requestItem-${requesterUuid}`).remove();
        loadReceivedFollowRequests(); // 요청 목록 새로고침
    } catch (e) {
        // 오류 처리
    }
}

/**
 * 4. 팔로우 요청 거절
 * @param {string} requesterUuid 요청자 UUID
 */
async function declineFollowRequest(requesterUuid) {
    try {
        await apiCall(`${BASE_URL}/decline/${requesterUuid}`, 'POST');
        alert(`${requesterUuid} 님의 팔로우 요청을 거절했습니다.`);

        // UI에서 해당 항목 제거 후 목록 새로고침
        document.getElementById(`requestItem-${requesterUuid}`).remove();
        loadReceivedFollowRequests(); // 요청 목록 새로고침
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

    console.log(user.userUuid + user.relationshipStatus);
    // 서버에서 반환한 relationshipStatus에 따라 버튼 상태 설정
    switch (user.relationshipStatus) {
        case 'FOLLOWING':
            buttonText = '언팔로우';
            buttonAction = 'unfollow';
            buttonClass = 'btn-success'; // 팔로우 중일 때는 다른 색상 (예: 초록색)
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

// 페이지 진입 시 첫 로딩
handleSearch();