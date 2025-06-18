const postGrid = document.getElementById('postGrid');
const searchInput = document.getElementById('searchInput');
const searchButton = document.getElementById('searchButton');
let page = 0;
let loading = false;
let finished = false;

// 현재 검색어 기억
let currentKeyword = '';

// 감시 대상
const observerTarget = document.getElementById('observerTarget');

// 프로필 이미지 설정 (초기 실행)
document.addEventListener("DOMContentLoaded", () => {
    const token = localStorage.getItem("token");
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

// 페이지 진입 시 첫 로딩
handleSearch();
