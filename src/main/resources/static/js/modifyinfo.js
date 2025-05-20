const token = 'Bearer ' + localStorage.getItem("token");

if (!token) {
    alert("로그인이 필요합니다.");
    window.location.href = "/login.html"
}

document.addEventListener("DOMContentLoaded", async () => {




    const preview = document.getElementById('previewImage');
    const uploadInput = document.getElementById('uploadImage');
    const defaultImages = document.querySelectorAll('.default-images img');
    const nicknameInput = document.getElementById("nickname");
    const phoneInput = document.getElementById("phone");
    const introInput = document.getElementById("introduction");

    const modifyForm = document.getElementById("modifyForm");

    // 🔹 기존 회원 정보 불러오기
    const response = await fetch("/users/info", {
        method: "GET",
        headers: { "Authorization": "Bearer " + token }
    });

    if (response.ok) {
        const data = await response.json();
        nicknameInput.value = data.nickname;
        phoneInput.value = data.phone || "";
        introInput.value = data.introduction || "";
        preview.src = data.profileImagePath;
    }

    // 🔹 이미지 업로드 미리보기
    uploadInput.addEventListener("change", () => {
        const file = uploadInput.files[0];
        if (file) {
            const reader = new FileReader();
            reader.onload = () => {
                preview.src = reader.result;
                clearSelected();
            };
            reader.readAsDataURL(file);
        }
    });

    // 🔹 기본 이미지 선택
    defaultImages.forEach(img => {
        img.addEventListener("click", () => {
            preview.src = img.src;
            uploadInput.value = "";
            clearSelected();
            img.classList.add("selected");
        });
    });

    function clearSelected() {
        defaultImages.forEach(img => img.classList.remove("selected"));
    }

    // 🔹 저장 버튼 동작
    modifyForm.addEventListener("submit", async (e) => {
        e.preventDefault();
        const file = uploadInput.files[0];
        const selectedDefault = document.querySelector('.default-images img.selected');

        if (file) {
            const formData = new FormData();
            formData.append("profileImage", file);
            formData.append("nickname", nicknameInput.value);
            formData.append("phone", phoneInput.value);
            formData.append("introduction", introInput.value);

            const res = await fetch("/users/update-profile", {
                method: "POST",
                headers: { "Authorization": "Bearer " + token },
                body: formData
            });

            if (res.ok) {
                alert("회원 정보가 수정되었습니다.");
                window.location.href = "/main.html";
            } else {
                alert("수정 실패");
            }

        } else if (selectedDefault) {
            const json = {
                nickname: nicknameInput.value,
                phone: phoneInput.value,
                introduction: introInput.value,
                profileImagePath: selectedDefault.src
            };

            const res = await fetch("/users/info", {
                method: "PUT",
                headers: {
                    "Authorization": "Bearer " + token,
                    "Content-Type": "application/json"
                },
                body: JSON.stringify(json)
            });

            if (res.ok) {
                alert("회원 정보가 수정되었습니다.");
                window.location.href = "/main.html";
            } else {
                alert("수정 실패");
            }
        } else {
            alert("프로필 이미지를 선택하거나 업로드해주세요.");
        }
    });
});
