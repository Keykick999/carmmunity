const token = localStorage.getItem("token");
const urlParams = new URLSearchParams(window.location.search);
const postId = urlParams.get("postId");
alert(postId);


// 게시글 정보 불러오기
function loadPostForEdit() {
    if (!postId || !token) {
        alert("잘못된 접근입니다.");
        window.location.href = "/postList";
        return;
    }

    fetch(`/posts/${postId}`, {
        method: 'GET',
        headers: {
            "Authorization": `Bearer ${token}`
        }
    })
        .then(res => res.json())
        .then(post => {
            document.getElementById("title").value = post.postTitle;
            document.getElementById("content").value = post.postContent;

            // 기존 이미지가 있다면 사용자에게 참고용 이미지 경로 표시 (선택사항)
            if (post.thumbnailPath) {
                const thumbPreview = document.createElement('p');
                thumbPreview.textContent = `현재 썸네일: ${post.thumbnailPath}`;
                document.getElementById("thumbnailImage").parentElement.appendChild(thumbPreview);
            }

            if (post.imagePath) {
                const imagePreview = document.createElement('p');
                imagePreview.textContent = `현재 본문 이미지: ${post.imagePath}`;
                document.getElementById("postImage").parentElement.appendChild(imagePreview);
            }
        })
        .catch(err => {
            console.error(err);
            alert("게시글 정보를 불러오는데 실패했습니다.");
            window.location.href = "/postList";
        });
}

// 수정 폼 전송 처리
document.getElementById("postForm").addEventListener("submit", function (e) {
    e.preventDefault();

    const formData = new FormData();

    const postData = {
        title: document.getElementById("title").value,
        content: document.getElementById("content").value,
        postType: "FREE"  // 고정값 유지
    };

    formData.append("postData", new Blob([JSON.stringify(postData)], { type: "application/json" }));

    const postImage = document.getElementById("postImage").files[0];
    const thumbnailImage = document.getElementById("thumbnailImage").files[0];

    if (postImage) formData.append("postImage", postImage);
    if (thumbnailImage) formData.append("thumbnailImage", thumbnailImage);

    fetch(`/posts/${postId}`, {
        method: 'PATCH',  // 또는 PUT
        headers: {
            Authorization: `Bearer ${token}`
        },
        body: formData
    })
        .then(res => {
            if (!res.ok) throw new Error("수정 실패");
            return res.text();
        })
        .then(msg => {
            alert("✅ 게시글이 수정되었습니다.");
            window.location.href = `/postDetail?postId=${postId}`;
        })
        .catch(err => {
            console.error(err);
            alert("❌ 게시글 수정 중 오류가 발생했습니다.");
        });
});

loadPostForEdit();