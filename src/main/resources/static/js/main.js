import * as THREE from 'three';
import { GLTFLoader } from 'GLTFLoader';
import { OrbitControls } from 'OrbitControls';

const token = 'Bearer ' + localStorage.getItem("token");
if (!token || token === "Bearer null") {
    alert("로그인이 필요합니다.");
    window.location.href = "/";
}

// 사용자 정보 렌더링
const nickname = localStorage.getItem("nickname");
const name = localStorage.getItem("name");
const email = localStorage.getItem("email");
const phone = localStorage.getItem("phone");
const intro = localStorage.getItem("intro") || "자기소개가 아직 없습니다.";

document.querySelector(".user-info h5").textContent = nickname;
document.querySelector(".user-info").innerHTML += `
  <p>이름: ${name}</p>
  <p>전화번호: ${phone}</p>
  <p>이메일: ${email}</p>
  <div class="user-intro">${intro}</div>
`;

const scene = new THREE.Scene();
scene.background = new THREE.Color(0xf5f6fa);

const loader = new THREE.TextureLoader();
loader.load('/garage.jpg', (texture) => {
    scene.background = texture;
});

const camera = new THREE.PerspectiveCamera(75, 900 / 500, 0.1, 1000);
camera.position.set(0, 2, 20);

const renderer = new THREE.WebGLRenderer({ antialias: true });
renderer.setSize(900, 500);
document.querySelector('.viewer-container').appendChild(renderer.domElement);

const controls = new OrbitControls(camera, renderer.domElement);
controls.enableDamping = true;

scene.add(new THREE.AmbientLight(0xffffff, 1.8));
const dirLight = new THREE.DirectionalLight(0xffffff, 1);
dirLight.position.set(5, 10, 7.5);
scene.add(dirLight);

let currentModel;
window.loadCar = function (slotId) {
    const modelUrl = `http://localhost:8080/cars?carId=${slotId}`;
    const loader = new GLTFLoader();

    fetch(modelUrl, {
        headers: { Authorization: token }
    })
        .then(res => {
            if (!res.ok) throw new Error("Fetch 실패: " + res.status);
            return res.arrayBuffer();
        })
        .then(buffer => {
            if (currentModel) scene.remove(currentModel);
            loader.parse(buffer, '', (gltf) => {
                currentModel = gltf.scene;
                scene.add(gltf.scene);
            });
        })
        .catch(err => console.error('모델 로딩 오류:', err));
};

function animate() {
    requestAnimationFrame(animate);
    controls.update();
    renderer.render(scene, camera);
}
animate();
loadCar(1);