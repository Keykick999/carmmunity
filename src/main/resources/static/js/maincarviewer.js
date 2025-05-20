import * as THREE from 'three';
import { GLTFLoader } from 'three/examples/jsm/loaders/GLTFLoader.js';
import { OrbitControls } from 'three/examples/jsm/controls/OrbitControls.js';

const container = document.querySelector('.viewer-container');

// 반응형 렌더링 크기
const width = container.clientWidth;
const height = container.clientHeight;

// 씬 설정
const scene = new THREE.Scene();
scene.background = new THREE.Color(0xf5f6fa);

// 카메라 설정
const camera = new THREE.PerspectiveCamera(50, width / height, 0.1, 1000);
camera.position.set(0, 2, 5);

// 렌더러 설정
const renderer = new THREE.WebGLRenderer({ antialias: true });
renderer.setSize(width, height);
renderer.setPixelRatio(window.devicePixelRatio);
container.appendChild(renderer.domElement);

// 조명 설정
const ambientLight = new THREE.AmbientLight(0xffffff, 1.2);
const directionalLight = new THREE.DirectionalLight(0xffffff, 1.0);
directionalLight.position.set(5, 10, 7.5);
scene.add(ambientLight, directionalLight);

// OrbitControls
const controls = new OrbitControls(camera, renderer.domElement);
controls.enableDamping = true;
controls.target.set(0, 1, 0); // 차량 중심
controls.minDistance = 3;
controls.maxDistance = 8;
controls.maxPolarAngle = Math.PI; // ✅ 위아래 자유 회전 허용

let currentModel = null;
const token = 'Bearer ' + localStorage.getItem("token");

// 차량 로딩 함수
function loadCar(slotId) {
    alert(slotId);
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
            if (currentModel) {
                scene.remove(currentModel);
                currentModel.traverse(child => {
                    if (child.isMesh) {
                        child.geometry.dispose();
                        if (child.material.map) child.material.map.dispose();
                        child.material.dispose();
                    }
                });
            }
            loader.parse(buffer, '', (gltf) => {
                currentModel = gltf.scene;
                currentModel.rotation.y = Math.PI;
                currentModel.position.set(0, 0.1, 0);
                scene.add(currentModel);
            });
        })
        .catch(err => {
            console.warn(`슬롯 ${slotId} 차량 로딩 실패`, err);
            alert(`슬롯 ${slotId}에 저장된 차량이 없습니다.`);
        });
}

// 애니메이션 루프
function animate() {
    requestAnimationFrame(animate);
    controls.update();
    renderer.render(scene, camera);
}
animate();

// 기본 차량 로딩 (slot 1)
loadCar(1);

// 반응형 리사이즈 대응
window.addEventListener('resize', () => {
    const width = container.clientWidth;
    const height = container.clientHeight;
    renderer.setSize(width, height);
    camera.aspect = width / height;
    camera.updateProjectionMatrix();
});
