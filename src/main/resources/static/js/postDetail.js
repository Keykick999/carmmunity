const postId = document.getElementById("postId").value;
const token = localStorage.getItem('token');

function loadUserProfileImage() {
    const token = localStorage.getItem("token");
    if (!token) return;

    fetch("/users/info", {
        method: "GET",
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
        .catch(err => {
            console.error("❌ 프로필 이미지 로딩 실패:", err);
        });
}


function escapeHtml(text) {
return $('<div>').text(text).html();
}

function loadPost() {
    const token = localStorage.getItem("token");

    $.ajax({
        url: `/posts/${postId}`,
        type: 'GET',
        headers: {
            'Authorization': `Bearer ${token}`
        },
        success: function (post) {
            console.log(post.imagePath);
            const imageTag = post.imagePath ? `<img class="post-image" src="${post.imagePath}" onerror="this.style.display='none'">` : '';

            const isLiked = post.likedByCurrentUser;
            const likeText = isLiked ? '💖 좋아요' : '❤ 좋아요';
            const likeDataAttr = isLiked ? 'true' : 'false';

            // 추가: 클래스 다르게 주고 싶으면 조건부 클래스 적용 가능
            const likeButtonStyle = isLiked
                ? 'background-color: #ffdddd; font-weight: bold;'
                : '';

            const postHtml = `
                <div class="post-title">${post.nickname}</div>
                ${imageTag}
                <div class="post-content">${escapeHtml(post.postContent)}</div>
                <button class="like-button" style="${likeButtonStyle}" data-liked="${likeDataAttr}" onclick="togglePostLike(this)">
                    ${likeText}
                </button>
            `;
            $('#post').html(postHtml);
        }
    });
}


function togglePostLike(buttonEl) {
    const isLiked = $(buttonEl).attr('data-liked') === 'true';
    $.ajax({
    url: `/posts/${postId}/like`,
    type: 'POST',
    headers: { 'Authorization': `Bearer ${token}`, 'Content-Type': 'application/json' },
    data: JSON.stringify({ likeStatus: !isLiked }),
    success: function (res) {
    $(buttonEl).attr('data-liked', res.currentState);
    $(buttonEl).text(res.currentState ? '💖 좋아요' : '❤ 좋아요');
}
});
}

function loadComments() {
    $.ajax({
        url: `/comments?postId=${postId}&page=0&size=100`,
        type: 'GET',
        headers: { 'Authorization': `Bearer ${token}` },
        success: function (response) {
            const commentsHtml = response.content.map(comment => {
                const isLiked = comment.likedByCurrentUser;
                const likeText = isLiked ? '💖 좋아요' : '❤ 좋아요';
                const likeDataAttr = isLiked ? 'true' : 'false';
                const likeButtonStyle = isLiked
                    ? 'background-color: #ffdddd; font-weight: bold;'
                    : '';

                return `
                    <div class="comment" data-comment-id="${comment.commentId}" data-nickname="${comment.nickName}">
                        <strong>${escapeHtml(comment.nickName)}</strong><br>
                        ${escapeHtml(comment.commentContent)}
                        <div class="reply-toggle" onclick="toggleReplies(this, ${comment.commentId}, '${comment.nickName}')">
                            답글 보기
                        </div>
                        <button class="like-button" style="${likeButtonStyle}" data-liked="${likeDataAttr}" onclick="toggleCommentLike(this, ${comment.commentId})">
                            ${likeText}
                        </button>
                        <div class="reply-list" style="display:none;"></div>
                        <div class="reply-input">
                            <input type="text" placeholder="답글달기">
                            <button onclick="submitReply(${comment.commentId}, '${comment.nickName}', this)">게시</button>
                        </div>
                    </div>
                `;
            }).join('');
            $('#comments').html(commentsHtml);
        }
    });
}

function toggleCommentLike(buttonEl, commentId) {
    const isLiked = $(buttonEl).attr('data-liked') === 'true';
    $.ajax({
    url: `/comments/${commentId}/like`,
    type: 'POST',
    headers: { 'Authorization': `Bearer ${token}`, 'Content-Type': 'application/json' },
    data: JSON.stringify({ likeStatus: !isLiked }),
    success: function (res) {
    $(buttonEl).attr('data-liked', res.currentState);
    $(buttonEl).text(res.currentState ? '💖 좋아요' : '❤ 좋아요');
}
});
}

    function toggleReplies(buttonEl, parentCommentId) {
    const $comment = $(buttonEl).closest('.comment');
    const $replyList = $comment.find('.reply-list');

    if ($replyList.is(':visible')) {
    $replyList.slideUp();
    $(buttonEl).text(`답글 보기`);
} else {
    $replyList.empty();
    loadReplies(parentCommentId, $replyList, 0);
    $replyList.slideDown();
    $(buttonEl).text(`답글 접기`);
}
}

function loadReplies(parentCommentId, $replyList, replyPage = 0) {
    $.ajax({
        url: `/comments?postId=${postId}&parentCommentId=${parentCommentId}&page=${replyPage}&size=5`,
        type: 'GET',
        headers: { 'Authorization': `Bearer ${token}` },
        success: function (response) {
            const repliesHtml = response.content.map(reply => {
                const isLiked = reply.likedByCurrentUser;
                const likeText = isLiked ? '💖 좋아요' : '❤ 좋아요';
                const likeDataAttr = isLiked ? 'true' : 'false';
                const likeButtonStyle = isLiked
                    ? 'background-color: #ffdddd; font-weight: bold;'
                    : '';

                return `
                    <div class="reply">
                        <strong>${escapeHtml(reply.nickName)}</strong><br>
                        ${escapeHtml(reply.commentContent)}
                        <button class="like-button" style="${likeButtonStyle}" data-liked="${likeDataAttr}" onclick="toggleCommentLike(this, ${reply.commentId})">
                            ${likeText}
                        </button>
                    </div>
                `;
            }).join('');

            let $section = $replyList.find('.reply-section');
            if ($section.length === 0) {
                $section = $('<div class="reply-section"></div>').appendTo($replyList);
            }
            $section.append(repliesHtml);

            $replyList.find('.reply-pagination').remove();
            if (!response.last) {
                const moreBtn = $(`<div class="reply-pagination">답글 더보기</div>`);
                moreBtn.on('click', function () {
                    loadReplies(parentCommentId, $replyList, replyPage + 1);
                });
                $replyList.append(moreBtn);
            }
        }
    });
}


function submitComment() {
    const content = $('#commentInput').val();
    if (!content) return;

    $.ajax({
    url: '/comments',
    type: 'POST',
    contentType: 'application/json',
    headers: { 'Authorization': `Bearer ${token}` },
    data: JSON.stringify({
    content: content,
    postId: postId,
    parentCommentId: null,
    mentionUserId: null
}),
    success: function () {
    $('#commentInput').val('');
    loadComments();
},
    error: function (xhr) {
    if (xhr.status === 401) {
    $('body').append(`
                        <div style="text-align:center; margin-top:20px;">
                            <p>로그인 후 이용해주세요.</p>
                            <a href="/" style="color:#00aaff; text-decoration:underline;">홈으로 이동</a>
                        </div>
                    `);
}
}
});
}

    function submitReply(parentCommentId, mentionNickName, buttonEl) {
    const $input = $(buttonEl).siblings('input');
    const content = $input.val();
    if (!content) return;

    $.ajax({
    url: '/comments',
    type: 'POST',
    contentType: 'application/json',
    headers: { 'Authorization': `Bearer ${token}` },
    data: JSON.stringify({
    content: `${content}`,
    postId: postId,
    parentCommentId: parentCommentId,
    mentionUserId: null
}),
    success: function () {
    $input.val('');
    const $replyList = $(buttonEl).closest('.comment').find('.reply-list').empty();
    loadReplies(parentCommentId, $replyList, 0);
},
    error: function (xhr) {
    if (xhr.status === 401) {
    $('body').append(`
                        <div style="text-align:center; margin-top:20px;">
                            <p>로그인 후 이용해주세요.</p>
                            <a href="/" style="color:#00aaff; text-decoration:underline;">홈으로 이동</a>
                        </div>
                    `);
}
}
});
}

    $(document).ready(function () {
    loadPost();
    loadComments();
    loadUserProfileImage();
});
