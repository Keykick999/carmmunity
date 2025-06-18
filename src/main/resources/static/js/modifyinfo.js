document.addEventListener("DOMContentLoaded", async () => {
    const rawToken = localStorage.getItem("token");

    if (!rawToken) {
        alert("로그인이 필요합니다.");
        window.location.href = "/login";
        return;
    }

    const token = 'Bearer ' + rawToken;

    const preview = document.getElementById('previewImage');
    const uploadInput = document.getElementById('uploadImage');
    const defaultImages = document.querySelectorAll('.default-images img');
    const nicknameInput = document.getElementById("nickname");
    const phoneInput = document.getElementById("phone");
    const introInput = document.getElementById("introduction");
    const modifyForm = document.getElementById("modifyForm");

    // 기존 회원 정보 불러오기
    try {
        const response = await fetch("/users/info", {
            method: "GET",
            headers: { "Authorization": token }
        });

        if (!response.ok) throw new Error("회원 정보를 불러올 수 없습니다.");

        const data = await response.json();
        nicknameInput.value = data.nickname;
        phoneInput.value = data.phone || "";
        introInput.value = data.introduction || "";
        preview.src = data.profileImagePath;
    } catch (err) {
        alert(err.message);
        return;
    }

    // 이미지 업로드 → 미리보기로 표시
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

    // 기본 이미지 클릭 시 미리보기로 반영
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

    // 저장 버튼 제출 처리
    modifyForm.addEventListener("submit", async (e) => {
        e.preventDefault();

        const file = uploadInput.files[0];
        const selectedDefault = document.querySelector('.default-images img.selected');

        // 업로드 이미지가 있는 경우
        if (file) {
            const formData = new FormData();
            formData.append("profileImage", file);
            formData.append("nickname", nicknameInput.value);
            formData.append("phone", phoneInput.value);
            formData.append("introduction", introInput.value);

            const res = await fetch("/users/update-profile", {
                method: "POST",
                headers: {
                    "Authorization": token
                },
                body: formData
            });

            if (res.ok) {
                alert("회원 정보가 수정되었습니다.");
                window.location.href = "/main";
            } else {
                alert("수정 실패");
            }

        }
        // 기본 이미지만 선택한 경우
        else if (selectedDefault) {
            const json = {
                nickname: nicknameInput.value,
                phone: phoneInput.value,
                introduction: introInput.value,
                profileImagePath: selectedDefault.src
            };

            const res = await fetch("/users/info", {
                method: "PUT",
                headers: {
                    "Authorization": token,
                    "Content-Type": "application/json"
                },
                body: JSON.stringify(json)
            });

            if (res.ok) {
                alert("회원 정보가 수정되었습니다.");
                window.location.href = "/main";
            } else {
                alert("수정 실패");
            }
        }
        // 이미지가 선택되지 않은 경우
        else {
            alert("프로필 이미지를 선택하거나 업로드해주세요.");
        }
    });
});
