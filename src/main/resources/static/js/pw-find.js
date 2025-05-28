function sendCode() {
    const email = $('#email').val();

    if (!email) {
        $('#emailError').text('이메일을 입력해주세요.').show();
        return;
    }

    $.ajax({
        type: 'POST',
        url: '/email/send',
        contentType: 'application/json',
        data: JSON.stringify({ email: email, purpose: 'reset' }),
        success: () => {
            $('#emailError').hide();
            showResultMessage('인증 코드가 이메일로 전송되었습니다.', true);
        },
        error: (xhr) => {
            $('#emailError').text(xhr.responseText || '인증 코드 전송 실패').show();
        }
    });
}

function verifyCode() {
    const email = $('#email').val();
    const code = $('#emailCode').val();

    if (!code) {
        showResultMessage('인증 코드를 입력해주세요.', false);
        return;
    }

    $.ajax({
        type: 'POST',
        url: '/email/verify',
        contentType: 'application/json',
        data: JSON.stringify({ email: email, code: code, purpose: 'reset' }),
        success: () => {
            showResultMessage('이메일 인증에 성공했습니다.', true);
        },
        error: (xhr) => {
            showResultMessage(xhr.responseText || '인증 실패', false);
        }
    });
}

function resetPassword() {
    const email = $('#email').val();
    const verificationCode = $('#emailCode').val();
    const newPassword = $('#newPassword').val();
    const confirmPassword = $('#confirmPassword').val();

    let valid = true;

    if (!newPassword || newPassword.length < 6) {
        $('#newPwError').text('비밀번호는 최소 6자 이상이어야 합니다.').show();
        valid = false;
    } else {
        $('#newPwError').hide();
    }

    if (newPassword !== confirmPassword) {
        $('#confirmPwError').text('비밀번호가 일치하지 않습니다.').show();
        valid = false;
    } else {
        $('#confirmPwError').hide();
    }

    if (!valid) return;

    $.ajax({
        type: 'POST',
        url: '/users/reset-password',
        contentType: 'application/json',
        data: JSON.stringify({
            email: email,
            verificationCode: verificationCode,
            newPassword: newPassword
        }),
        success: () => {
            showResultMessage('비밀번호가 성공적으로 변경되었습니다.', true);
        },
        error: (xhr) => {
            showResultMessage(xhr.responseText || '비밀번호 변경 실패', false);
        }
    });
}

function showResultMessage(message, isSuccess) {
    const resultBox = $('#resultMessage');
    resultBox
        .removeClass('success error')
        .addClass(isSuccess ? 'success' : 'error')
        .text(message)
        .fadeIn();
}
