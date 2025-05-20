// postlist.js
const postGrid = document.getElementById('postGrid');
const searchInput = document.getElementById('searchInput');
const searchButton = document.getElementById('searchButton');
let page = 0;
let loading = false;
let finished = false;

// 검색 버튼 클릭 또는 엔터 입력 시 검색 실행
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
    fetchPosts();
}

window.addEventListener('scroll', () => {
    if (window.innerHeight + window.scrollY >= document.body.offsetHeight - 100 && !loading && !finished) {
        fetchPosts();
    }
});

function fetchPosts() {
    loading = true;
    const keyword = searchInput.value.trim();
    const params = new URLSearchParams({
        page,
        size: 6,
        title: keyword
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
        .catch(err => console.error(err))
        .finally(() => loading = false);
}

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

// 초기 로딩
fetchPosts();
