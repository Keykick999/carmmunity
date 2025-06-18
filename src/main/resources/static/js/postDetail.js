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
            const imageTag = post.imagePath
                ? `<img class="post-image" src="${post.imagePath}" onerror="this.style.display='none'">`
                : '';

            const isLiked = post.likedByCurrentUser;
            const likeDataAttr = isLiked ? 'true' : 'false';
            const likeButtonHtml = `
            <button class="yt-like-button" data-liked="${likeDataAttr}" onclick="togglePostLike(this)">
                <i class="bi bi-hand-thumbs-up${isLiked ? '-fill' : ''}"></i> 좋아요
            </button>
        `;

                // 작성자라면 수정/삭제 버튼 추가
                const authorButtons = post.authorFlag
                    ? `
                <div class="mt-2">
                    <button class="btn btn-outline-primary btn-sm me-2" onclick="editPost(${post.postId})">수정</button>
                    <button class="btn btn-outline-danger btn-sm" onclick="deletePost(${post.postId})">삭제</button>
                </div>` : '';

                const postHtml = `
            <div class="post-title">${post.nickname}</div>
            ${imageTag}
            <div class="post-content">${escapeHtml(post.postContent)}</div>
            ${likeButtonHtml}
            ${authorButtons}
        `;

            $('#post').html(postHtml);
        },
        error: function (xhr) {
            if (xhr.status === 404) {
                alert("존재하지 않는 게시글입니다.");
            } else {
                alert("게시글을 불러오는 중 오류가 발생했습니다.")
            }
            window.location.href = "/postList";
        }
    });
}


function editPost(postId) {
    $.ajax({
        url: `/posts/${postId}`,
        type: 'GET',
        headers: { 'Authorization': `Bearer ${token}` },
        success: function () {
            window.location.href = `/edit-post?postId=${postId}`;  // 수정 폼 HTML 경로
        },
        error: function () {
            alert("게시글 데이터를 불러오지 못했습니다.");
        }
    });
}



function deletePost(postId) {
    if (!confirm("정말로 이 게시글을 삭제하시겠습니까?")) return;

    $.ajax({
        url: `/posts/${postId}`,
        type: 'DELETE',
        headers: { 'Authorization': `Bearer ${token}` },
        success: function () {
            alert("🗑️ 게시글이 삭제되었습니다.");
            window.location.href = "/postList";
        },
        error: function (xhr) {
            if (xhr.status === 403) {
                alert("❌ 삭제 권한이 없습니다.");
            } else {
                alert("❌ 게시글 삭제에 실패했습니다.");
            }
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
            $(buttonEl).html(`
             <i class="bi bi-hand-thumbs-up${res.currentState ? '-fill' : ''}"></i> 좋아요
            `);

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
                const likeDataAttr = isLiked ? 'true' : 'false';
                const likeButtonHtml = `
                  <button class="yt-like-button" data-liked="${likeDataAttr}" onclick="toggleCommentLike(this, ${comment.commentId})">
                    <i class="bi bi-hand-thumbs-up${isLiked ? '-fill' : ''}"></i> 좋아요
                  </button>
                `;

                const likeButtonStyle = isLiked ? 'background-color: #ffdddd; font-weight: bold;' : '';

                const authorButtons = comment.authorFlag
                    ? `
                        <button class="btn btn-sm btn-outline-primary me-2" onclick="editComment(${comment.commentId})">수정</button>
                        <button class="btn btn-sm btn-outline-danger" onclick="deleteComment(${comment.commentId})">삭제</button>
                      `
                    : '';

                return `
                    <div class="comment" data-comment-id="${comment.commentId}" data-nickname="${comment.nickName}">
                        <strong>${escapeHtml(comment.nickName)}</strong><br>
                        <div class="comment-content">${escapeHtml(comment.commentContent)}</div>
                        <div class="reply-toggle" onclick="toggleReplies(this, ${comment.commentId}, '${comment.nickName}')">
                            답글 보기
                        </div>
                        ${likeButtonHtml}
                        ${authorButtons}
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
            $(buttonEl)
                .attr('data-liked', res.currentState)
                .html(`
            <i class="bi bi-hand-thumbs-up${res.currentState ? '-fill' : ''}"></i> 좋아요
          `);
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
                const likeDataAttr = isLiked ? 'true' : 'false';
                const likeButtonHtml = `
                    <button class="yt-like-button" data-liked="${likeDataAttr}" onclick="toggleCommentLike(this, ${reply.commentId})">
                        <i class="bi bi-hand-thumbs-up${isLiked ? '-fill' : ''}"></i> 좋아요
                    </button>
                `;

                const authorButtons = reply.authorFlag
                    ? `
                        <button class="btn btn-sm btn-outline-primary me-2" onclick="editReply(${reply.commentId}, this)">수정</button>
                        <button class="btn btn-sm btn-outline-danger" onclick="deleteReply(${reply.commentId})">삭제</button>
                    `
                    : '';

                return `
                    <div class="reply" data-reply-id="${reply.commentId}">
                        <strong>${escapeHtml(reply.nickName)}</strong><br>
                        <div class="reply-content">${escapeHtml(reply.commentContent)}</div>
                        <div class="edit-reply-wrapper"></div>
                        ${likeButtonHtml}
                        ${authorButtons}
                    </div>
                `;
            }).join('');

            if (repliesHtml.trim() !== '') {
                let $section = $replyList.find('.reply-section');
                if ($section.length === 0) {
                    $section = $('<div class="reply-section"></div>').appendTo($replyList);
                }
                $section.append(repliesHtml);
            }

            $replyList.find('.reply-pagination').remove();

            if (!response.last && response.content.length > 0) {
                const moreBtn = $(`<div class="reply-pagination">답글 더보기</div>`);
                moreBtn.on('click', function () {
                    loadReplies(parentCommentId, $replyList, replyPage + 1);
                });
                $replyList.append(moreBtn);
            }
        }
    });
}


function editReply(replyId, buttonEl) {
    const $reply = $(buttonEl).closest('.reply');
    const $contentDiv = $reply.find('.reply-content');
    const $wrapper = $reply.find('.edit-reply-wrapper');

    if ($wrapper.children().length > 0) return;

    const originalContent = $contentDiv.text().trim();
    $contentDiv.hide();

    const $input = $(`<input type="text" class="edit-input form-control mb-2" value="${escapeHtml(originalContent)}">`);
    const $spinner = $(`<div class="spinner-border text-primary mt-2 ms-2" style="display:none; width: 1.5rem; height: 1.5rem;" role="status"><span class="visually-hidden">Loading...</span></div>`);
    const $saveBtn = $(`<button class="btn btn-outline-primary btn-sm me-2">저장</button>`);
    const $cancelBtn = $(`<button class="btn btn-outline-secondary btn-sm">취소</button>`);

    const $btnGroup = $('<div class="d-flex align-items-center mt-2"></div>')
        .append($saveBtn)
        .append($cancelBtn)
        .append($spinner);

    $wrapper.append($input).append($btnGroup);

    $saveBtn.on('click', function () {
        const newContent = $input.val().trim();
        if (!newContent || newContent === originalContent) return;

        $saveBtn.hide();
        $cancelBtn.hide();
        $spinner.show();

        $.ajax({
            url: `/comments/${replyId}`,
            type: 'PATCH',
            contentType: 'application/json',
            headers: { 'Authorization': `Bearer ${token}` },
            data: JSON.stringify({ content: newContent }),
            success: function () {
                $contentDiv.text(newContent).show();
                $wrapper.empty();
            },
            error: function () {
                $spinner.hide();
                $saveBtn.show();
                $cancelBtn.show();
                alert("❌ 답글 수정에 실패했습니다.");
            }
        });
    });

    $cancelBtn.on('click', function () {
        $wrapper.empty();
        $contentDiv.show();
    });
}

function deleteReply(replyId) {
    if (!confirm("정말로 이 답글을 삭제하시겠습니까?")) return;

    $.ajax({
        url: `/comments/${replyId}`,
        type: 'DELETE',
        headers: { 'Authorization': `Bearer ${token}` },
        success: function () {
            alert("🗑️ 답글이 삭제되었습니다.");
            $(`.reply[data-reply-id="${replyId}"]`).remove();
        },
        error: function () {
            alert("❌ 답글 삭제에 실패했습니다.");
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

function editComment(commentId) {
    const $comment = $(`[data-comment-id="${commentId}"]`);
    const $contentDiv = $comment.find('.comment-content');

    if ($comment.find('.edit-input').length > 0) return;

    const originalContent = $contentDiv.text().trim();
    $contentDiv.hide();

    const $input = $(`<input type="text" class="edit-input form-control mb-2" value="${escapeHtml(originalContent)}">`);

    const $spinner = $(`
        <div class="spinner-border text-primary mt-2 ms-2" style="display:none; width: 1.5rem; height: 1.5rem;" role="status">
            <span class="visually-hidden">Loading...</span>
        </div>
    `);

    const $saveBtn = $(`<button class="btn btn-outline-primary btn-sm me-2" style="width: 8%;">저장</button>`);
    const $cancelBtn = $(`<button class="btn btn-outline-secondary btn-sm" style="width: 8%;">취소</button>`);

    const $btnGroup = $('<div class="d-flex align-items-center mt-2"></div>')
        .append($saveBtn)
        .append($cancelBtn)
        .append($spinner);

    const $editWrapper = $('<div class="edit-area mt-2"></div>')
        .append($input)
        .append($btnGroup);

    $contentDiv.after($editWrapper);

    $saveBtn.on('click', function () {
        const newContent = $input.val().trim();
        if (!newContent || newContent === originalContent) return;

        $saveBtn.hide();
        $cancelBtn.hide();
        $spinner.show();

        $.ajax({
            url: `/comments/${commentId}`,
            type: 'PATCH',
            contentType: 'application/json',
            headers: { 'Authorization': `Bearer ${token}` },
            data: JSON.stringify({ content: newContent }),
            success: function () {
                $contentDiv.text(newContent).show();
                $editWrapper.remove();
            },
            error: function () {
                $spinner.hide();
                $saveBtn.show();
                $cancelBtn.show();
                alert("❌ 댓글 수정에 실패했습니다.");
            }
        });
    });

    $cancelBtn.on('click', function () {
        $editWrapper.remove();
        $contentDiv.show();
    });
}



function deleteComment(commentId) {
    if (!confirm("정말로 이 댓글을 삭제하시겠습니까?")) return;

    $.ajax({
        url: `/comments/${commentId}`,
        type: 'DELETE',
        headers: { 'Authorization': `Bearer ${token}` },
        success: function () {
            alert("🗑️ 댓글이 삭제되었습니다.");
            loadComments();
        },
        error: function () {
            alert("❌ 댓글 삭제에 실패했습니다.");
        }
    });
}

$(document).ready(function () {
    loadPost();
    loadComments();
    loadUserProfileImage();
});
