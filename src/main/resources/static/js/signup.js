
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
}

    // 이메일 인증 발송 (주석 처리됨)
    /*
    async function sendCode() {
    const email = document.getElementById("email").value;
    clearErrors();
    if (!isValidEmail(email)) {
    document.getElementById("emailError").innerText = "유효한 이메일 형식이 아닙니다.";
    return;
}

    const res = await fetch("/api/email/send-code", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ email, purpose: "signup" })
});

    alert(res.ok ? "인증 코드가 발송되었습니다." : "이메일 발송 실패");
}

    async function verifyCode() {
    const email = document.getElementById("email").value;
    const code = document.getElementById("emailCode").value;
    const res = await fetch("/api/email/verify-code", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ email, code, purpose: "signup" })
});
    alert(res.ok ? "이메일 인증 완료" : "코드가 올바르지 않습니다.");
}
    */

    async function signup() {
    clearErrors();
    const userId = document.getElementById("userId").value;
    const password = document.getElementById("password").value;
    const email = document.getElementById("email").value;
    const nickname = document.getElementById("nickname").value;

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

    if (nickname.trim().length < 2) {
    document.getElementById("nicknameError").innerText = "닉네임은 2자 이상이어야 합니다.";
    hasError = true;
}

    if (hasError) return;

    const data = {
    userId,
    password,
    name: document.getElementById("name").value,
    nickname,
    email,
    phone: document.getElementById("phone").value,
    agreedToTerms: document.getElementById("agreeTerms").checked
};

    const res = await fetch("/users", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(data)
});

    if (res.ok) {
    alert("회원가입 성공! 로그인 페이지로 이동합니다.");
    window.location.href = "/";
} else {
    alert("회원가입 실패. 아이디 또는 닉네임 중복을 확인해주세요.");
}
}
