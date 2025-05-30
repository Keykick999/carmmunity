const token = 'Bearer ' + localStorage.getItem("token");

if (!token || token === "Bearer null") {
    alert("로그인이 필요합니다.");
    window.location.href = "/";
}

document.addEventListener("DOMContentLoaded", () => {
    fetch("/users/info", {
        headers: {
            Authorization: token
        }
    })
        .then(res => {
            if (!res.ok) throw new Error("회원 정보 조회 실패");
            return res.json();
        })
        .then(user => {
            document.getElementById("profile-image").src = user.profileImagePath;
            document.getElementById("nickname").textContent = user.nickname;
            document.getElementById("name").textContent = user.name;
            document.getElementById("phone").textContent = user.phone;
            document.getElementById("email").textContent = user.email;
            document.getElementById("introduction").textContent = user.introduction;
        })
        .catch(err => {
            console.error("회원 정보 로딩 실패:", err);
            alert("회원 정보를 불러오는 데 실패했습니다.");
        });
});