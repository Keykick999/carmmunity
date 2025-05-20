const form = document.getElementById('postForm');
form.addEventListener('submit', function (e) {
    e.preventDefault();

    const token = localStorage.getItem('token');
    if (!token) {
        alert("로그인이 필요합니다.");
        window.location.href = "/";
        return;
    }

    const formData = new FormData();

    // 게시글 데이터 + 고정 postType
    const postData = {
        title: document.getElementById("title").value,
        content: document.getElementById("content").value,
        postType: "FREE"
    };
    formData.append("postData", new Blob(
        [JSON.stringify(postData)],
        { type: "application/json" }
    ));

    // 이미지 파일 추가
    const postImage = document.getElementById("postImage").files[0];
    const thumbnailImage = document.getElementById("thumbnailImage").files[0];

    if (postImage) {
        formData.append("postImage", postImage);
    }
    if (thumbnailImage) {
        formData.append("thumbnailImage", thumbnailImage);
    }

    fetch('/posts', {
        method: 'POST',
        headers: {
            Authorization: `Bearer ${token}`
        },
        body: formData
    })
        .then(res => {
            if (res.status === 401) {
                alert("로그인 후 이용해주세요.");
                window.location.href = "/"; // 로그인 페이지 또는 홈으로 이동
                return;
            }
            if (!res.ok) throw new Error("게시글 작성 실패");
            return res.text();
        })
        .then(msg => {
            if (msg) {  // 위에서 redirect된 경우엔 null이 넘어올 수 있음
                alert(msg);
                window.location.href = "/postList";
            }
        })
        .catch(err => {
            console.error(err);
            alert("작성 중 오류가 발생했습니다.");
        });
});