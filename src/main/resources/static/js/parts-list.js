
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
        .then(res => res.json())
        .then(user => {
            const profileImg = document.getElementById("profile-image");
            if (user.profileImagePath) {
                profileImg.src = user.profileImagePath;
            }
        });

    renderCart();
});

function renderCart() {
    fetch("/shopping/list", {
        headers: { "Authorization": token }
    })
        .then(res => res.json())
        .then(items => {
            const list = document.getElementById("cartItems");
            list.innerHTML = items.map(item => `
            <li>${item.label}
                <button onclick="removeFromCart('${item.code}')"
                        style="float:right; border:none; background:none; color:red; cursor:pointer;">✕</button>
            </li>
        `).join("");
        });
}

function removeFromCart(code) {
    fetch("/shopping/remove", {
        method: "POST",
        headers: {
            "Content-Type": "application/json",
            "Authorization": token
        },
        body: JSON.stringify({ imageCode: code })
    }).then(renderCart);
}