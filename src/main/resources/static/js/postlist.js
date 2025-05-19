const postGrid = document.getElementById('postGrid');
const searchInput = document.getElementById('searchInput');
let page = 0;
let loading = false;
let finished = false;

const token = 'Bearer ' + localStorage.getItem("token");
if (!token || token === "Bearer null") {
    alert("로그인이 필요합니다.");
    window.location.href = "/";
}

// 검색 키워드 기반 필터링
searchInput.addEventListener('input', () => {
    page = 0;
    finished = false;
    postGrid.innerHTML = '';
    fetchPosts();
});

// 스크롤 이벤트로 무한 스크롤 구현
window.addEventListener('scroll', () => {
    if (window.innerHeight + window.scrollY >= document.body.offsetHeight - 100 && !loading && !finished) {
        fetchPosts();
    }
});

function fetchPosts() {
    loading = true;
    const keyword = searchInput.value.trim();

    fetch(`/posts?page=${page}&size=6&keyword=${encodeURIComponent(keyword)}`, {
        headers: {
            Authorization: 'Bearer ' + token
        }
    })
        .then(res => {
            if (!res.ok) throw new Error('게시글 로딩 실패');
            return res.json();
        })
        .then(data => {
            if (data.length === 0) {
                finished = true;
                return;
            }
            data.forEach(post => renderPostCard(post));
            page++;
        })
        .catch(err => console.error(err))
        .finally(() => loading = false);
}

function renderPostCard(post) {
    const card = document.createElement('div');
    card.className = 'post-card';
    card.onclick = () => {
        location.href = `/postdetail.html?postId=${post.id}`;
    };

    card.innerHTML = `
    <img class="thumbnail" src="${post.thumbnailUrl || '/assets/default-thumbnail.jpg'}" alt="썸네일">
    <div class="post-content">
      <div class="post-user">
        <img src="${post.userProfileUrl || '/assets/default-profile.png'}" alt="프로필">
        <span>${post.nickname}</span>
      </div>
      <div class="post-title">${post.title}</div>
      <div class="post-meta">
        <span>❤ ${post.likeCount}</span>
        <span>${post.updatedAt || post.createdAt}</span>
      </div>
    </div>
  `;

    postGrid.appendChild(card);
}

// 초기 로딩
fetchPosts();
