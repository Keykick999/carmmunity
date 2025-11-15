function handleSearch() {
    appState.page = 0;
    appState.finished = false;
    appState.postGrid.innerHTML = '';
    appState.currentKeyword = appState.searchInput.value.trim();
    fetchPosts();
}

// IntersectionObserver 설정
const observer = new IntersectionObserver((entries) => {
    if (entries[0].isIntersecting && !appState.loading && !appState.finished) {
        fetchPosts();
    }
}, {
    root: null,
    threshold: 0.1
});

// 게시글 불러오기
function fetchPosts() {
    appState.loading = true;

    const params = new URLSearchParams({
        page: appState.page,
        size: 6,
        title: appState.currentKeyword
    });

    fetch(`/posts?${params.toString()}`)
        .then(res => {
            if (!res.ok) throw new Error('게시글 로딩 실패');
            return res.json();
        })
        .then(data => {
            const posts = data.content;

            if (posts.length === 0) {
                appState.finished = true;
                return;
            }

            posts.forEach(renderPostCard);
            appState.page++;
        })
        .catch(err => console.error('❌ 게시글 불러오기 실패:', err))
        .finally(() => appState.loading = false);
}

