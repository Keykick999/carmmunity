const token = 'Bearer ' + localStorage.getItem("token");

if (!token || token === "Bearer null") {
    alert("로그인이 필요합니다.");
    window.location.href = "/";
}

// 사용자 프로필 로딩
document.addEventListener("DOMContentLoaded", () => {
    fetch("/users/info", { headers: { Authorization: token } })
        .then(res => res.json())
        .then(user => {
            const profileImg = document.getElementById("profile-image");
            if (user.profileImagePath) {
                profileImg.src = user.profileImagePath;
            }
        })
        .catch(err => {
            console.error("회원 정보 로딩 실패:", err);
        });

    initSelectBoxes();
});

// 초기 셀렉트 박스 구성
function initSelectBoxes() {
    const manufacturerSelect = document.getElementById("manufacturerSelect");
    const modelSelect = document.getElementById("modelSelect");
    const codeNameSelect = document.getElementById("codeNameSelect");

    const modelMap = {
        bmw: ["1시리즈", "2시리즈", "3시리즈", "4시리즈", "M2", "M3", "M4"],
        benz: ["A클래스", "C클래스", "E클래스"],
        audi: ["A3", "A5", "A6", "A7"]
    };

    const codeNameMap = {
        M4: ["g82", "f82", "e92"],
        M3: ["g80", "f80", "e90"]
    };

    manufacturerSelect.addEventListener("change", () => {
        const selectedManu = manufacturerSelect.value;
        modelSelect.innerHTML = `<option value="">차량명을 선택하세요</option>`;
        codeNameSelect.innerHTML = `<option value="">코드네임을 선택하세요</option>`;
        codeNameSelect.disabled = true;
        document.getElementById("searchBtn").disabled = true;

        if (selectedManu) {
            modelSelect.disabled = false;
            (modelMap[selectedManu] || []).forEach(model => {
                const opt = document.createElement("option");
                opt.value = model;
                opt.textContent = model;
                modelSelect.appendChild(opt);
            });
        } else {
            modelSelect.disabled = true;
        }
    });

    modelSelect.addEventListener("change", () => {
        const selectedModel = modelSelect.value;
        codeNameSelect.innerHTML = `<option value="">코드네임을 선택하세요</option>`;
        document.getElementById("searchBtn").disabled = true;

        if (selectedModel && codeNameMap[selectedModel]) {
            codeNameSelect.disabled = false;
            codeNameMap[selectedModel].forEach(code => {
                const opt = document.createElement("option");
                opt.value = code;
                opt.textContent = code.toUpperCase();
                codeNameSelect.appendChild(opt);
            });
        } else {
            codeNameSelect.disabled = true;
        }
    });

    codeNameSelect.addEventListener("change", () => {
        const valid = manufacturerSelect.value && modelSelect.value && codeNameSelect.value;
        document.getElementById("searchBtn").disabled = !valid;
    });
}

// 차량 검색
async function searchCarImages() {
    const manu = document.getElementById("manufacturerSelect").value.toLowerCase();
    const model = document.getElementById("modelSelect").value.toLowerCase();
    const code = document.getElementById("codeNameSelect").value.toLowerCase();

    const response = await fetch(`/tuning/cars?manufacturer=${manu}&model=${model}&codeName=${code}`);
    const imageNames = await response.json();
    console.log(imageNames);

    const container = document.getElementById("imageContainer");
    container.innerHTML = "";

    const cart = JSON.parse(localStorage.getItem("carCart") || "{}");
    const selectedCar = cart["car"];

    imageNames.forEach(name => {
        const card = document.createElement("div");
        card.className = "car-card";

        const img = document.createElement("img");
        console.log(`/uploads/2d/${manu}/${model}/${code}/cars/${name}`);
        img.src = `/uploads/2d/${manu}/${model}/${code}/cars/${name}`;
        img.alt = name;

        const label = document.createElement("div");
        label.className = "car-name";
        label.textContent = name.replace(".png", "");

        if (selectedCar === name.replace(".png", "")) {
            card.classList.add("selected");
        }

        card.appendChild(img);
        card.appendChild(label);

        card.onclick = () => {
            const cart = JSON.parse(localStorage.getItem("carCart") || "{}");
            const current = cart["car"];
            const clicked = name.replace(".png", "");
            const isSame = current === clicked;

            if (isSame) {
                if (confirm("선택된 차량을 삭제하시겠습니까?")) {
                    localStorage.removeItem("carCart");
                    card.classList.remove("selected");
                }
                // 아니오: 아무 것도 하지 않음
            } else if (current) {
                if (confirm("이미 차량이 선택되어 있습니다. 차량을 변경하시겠습니까?")) {
                    document.querySelectorAll(".car-card").forEach(c => c.classList.remove("selected"));
                    card.classList.add("selected");
                    localStorage.setItem("carCart", JSON.stringify({ car: clicked }));
                    localStorage.setItem("selectedManufacturer", manu);
                    localStorage.setItem("selectedModel", model);
                    localStorage.setItem("selectedCodeName", code);
                }
                // 아니오: 아무 것도 하지 않음
            } else {
                card.classList.add("selected");
                localStorage.setItem("carCart", JSON.stringify({ car: clicked }));
                localStorage.setItem("selectedManufacturer", manu);
                localStorage.setItem("selectedModel", model);
                localStorage.setItem("selectedCodeName", code);
            }
        };

        container.appendChild(card);
    });
}

function goToCartPage() {
    window.location.href = "/shopping-list";
}

function goToPartsList() {
    const cart = JSON.parse(localStorage.getItem("carCart") || "{}");
    const imageCode = cart["car"];

    if (!imageCode) {
        alert("차량을 먼저 선택해주세요.");
        return;
    }

    fetch("/shopping/add", {
        method: "POST",
        headers: {
            "Content-Type": "application/json",
            "Authorization": token
        },
        body: JSON.stringify({ imageCode })
    })
        .then(res => {
            if (!res.ok) throw new Error("장바구니 등록 실패");
            window.location.href = "/parts-list";
        })
        .catch(err => {
            console.error("등록 실패:", err);
            alert("차량 등록에 실패했습니다.");
        });
}

function toggleCartBox() {
    const box = document.getElementById("cartBox");
    box.style.display = box.style.display === "block" ? "none" : "block";
    renderCart();
}

function renderCart() {
    const list = document.getElementById("cartItems");
    const cart = JSON.parse(localStorage.getItem("carCart") || "{}");

    list.innerHTML = Object.entries(cart).map(([key, value]) => `
    <li>
        ${key}: ${value}
        <button onclick="removeFromCart('${key}')" style="float: right; border: none; background: none; color: red; cursor: pointer;">✕</button>
    </li>
    `).join("");
}

function removeFromCart(key) {
    const cart = JSON.parse(localStorage.getItem("carCart") || "{}");
    delete cart[key];
    localStorage.setItem("carCart", JSON.stringify(cart));
    renderCart();
}

function clearCart() {
    if (confirm("장바구니를 모두 \?")) {
        localStorage.removeItem("carCart");
        renderCart();
    }
}