function clearErrors() {
    $('.error-message').hide().text('');
    $('#resultMessage').hide().removeClass('success error').text('');
}

function findId() {
    clearErrors();

    const email = $('#email').val();
    let hasError = false;

    // 이메일 유효성 검사
    if (!email) {
        $('#emailError').text('이메일을 입력해주세요.').show();
        hasError = true;
    } else {
        const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        if (!emailRegex.test(email)) {
            $('#emailError').text('올바른 이메일 형식이 아닙니다.').show();
            hasError = true;
        }
    }

    if (hasError) return;

    $.ajax({
        url: '/users/find-id',
        method: 'POST',
        contentType: 'application/json',
        data: JSON.stringify({
            email: email
        }),
        success: function(response) {
            $('#resultMessage')
                .text(`회원님의 아이디는 ${response.userId} 입니다.`)
                .addClass('success')
                .show();
        },
        error: function(xhr) {
            if (xhr.status === 404) {
                $('#resultMessage')
                    .text('해당 이메일로 등록된 계정이 없습니다.')
                    .addClass('error')
                    .show();
            } else {
                $('#resultMessage')
                    .text('아이디 찾기 중 오류가 발생했습니다.')
                    .addClass('error')
                    .show();
            }
        }
    });
}

// Enter 키 이벤트 처리
$('#email').keypress(function(e) {
    if (e.which === 13) {
        findId();
    }
});