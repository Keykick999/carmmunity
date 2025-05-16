let isEmailVerified = false;

function isValidUserId(id) {
    return /^[a-zA-Z0-9]{5,20}$/.test(id);
}

function isValidPassword(pw) {
    return /^(?=.*[a-zA-Z])(?=.*\d)(?=.*[!@#$%^&*()_+]).{8,20}$/.test(pw);
}

function isValidEmail(email) {
    return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email);
}

function clearErrors() {
    document.getElementById("userIdError").innerText = "";
    document.getElementById("passwordError").innerText = "";
    document.getElementById("emailError").innerText = "";
    document.getElementById("nicknameError").innerText = "";
    document.getElementById("nameError").innerText = "";
}

async function sendCode() {
    clearErrors();
    const email = document.getElementById("email").value;

    if (!isValidEmail(email)) {
        document.getElementById("emailError").innerText = "유효한 이메일 형식이 아닙니다.";
        return;
    }

    try {
        const res = await fetch("/email/send", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ email, purpose: "signup" })
        });

        alert(res.ok ? "인증 코드가 발송되었습니다." : "이메일 발송 실패");
    } catch (error) {
        alert("인증 코드 발송 중 오류가 발생했습니다.");
    }
}

async function verifyCode() {
    const email = document.getElementById("email").value;
    const code = document.getElementById("emailCode").value;

    if (!code) {
        alert("인증 코드를 입력해주세요.");
        return;
    }

    try {
        const res = await fetch("/email/verify", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ email, code, purpose: "signup" })
        });

        if (res.ok) {
            isEmailVerified = true;
            document.getElementById("emailError").innerText = "";
            alert("이메일 인증 완료");
        } else {
            isEmailVerified = false;
            alert("코드가 올바르지 않습니다.");
        }
    } catch (error) {
        alert("인증 확인 중 오류가 발생했습니다.");
    }
}

async function signUp() {
    clearErrors();

    const userId = document.getElementById("userId").value.trim();
    const password = document.getElementById("password").value;
    const email = document.getElementById("email").value.trim();
    const name = document.getElementById("name").value.trim();
    const nickname = document.getElementById("nickname").value.trim();
    const phone = document.getElementById("phone").value.trim();
    const verificationCode = document.getElementById("emailCode").value.trim();
    const agreedToTerms = document.getElementById("agreeTerms").checked;

    let hasError = false;

    if (!isValidUserId(userId)) {
        document.getElementById("userIdError").innerText = "아이디는 영문자+숫자 조합의 5~20자여야 합니다.";
        hasError = true;
    }

    if (!isValidPassword(password)) {
        document.getElementById("passwordError").innerText = "비밀번호는 영문자+숫자+특수문자 조합의 8~20자여야 합니다.";
        hasError = true;
    }

    if (!isValidEmail(email)) {
        document.getElementById("emailError").innerText = "유효한 이메일 형식이 아닙니다.";
        hasError = true;
    }

    if (nickname.length < 2) {
        document.getElementById("nicknameError").innerText = "닉네임은 2자 이상이어야 합니다.";
        hasError = true;
    }

    if (!name) {
        document.getElementById("nameError").innerText = "이름을 입력해주세요.";
        hasError = true;
    }

    if (!verificationCode) {
        document.getElementById("emailError").innerText = "이메일 인증 코드를 입력해주세요.";
        hasError = true;
    }

    if (!isEmailVerified) {
        document.getElementById("emailError").innerText = "이메일 인증을 완료해주세요.";
        hasError = true;
    }

    if (!agreedToTerms) {
        alert("약관에 동의해야 가입할 수 있습니다.");
        hasError = true;
    }

    if (hasError) return;

    const data = {
        userId,
        password,
        email,
        name,
        nickname,
        phone,
        verificationCode
    };

    try {
        const res = await fetch("/users", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(data)
        });

        if (res.ok) {
            alert("회원가입 성공! 로그인 페이지로 이동합니다.");
            window.location.href = "/";
        } else if (res.status === 409) {
            alert("회원가입 실패. 아이디 또는 닉네임 중복입니다.");
        } else {
            alert("회원가입 실패. 다시 시도해주세요.");
        }
    } catch (error) {
        alert("회원가입 요청 중 오류가 발생했습니다.");
    }
}