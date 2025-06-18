import * as THREE from 'three';
import { GLTFLoader } from 'GLTFLoader';
import { OrbitControls } from 'OrbitControls';

const container = document.querySelector('.viewer-container');
const width = container.clientWidth;
const height = container.clientHeight;
const aspect = width / height;

// 씬 생성
const scene = new THREE.Scene();
// scene.background = new THREE.Color(0xf5f6fa);
scene.background = new THREE.Color(0xffffff); //


// OrthographicCamera 사용 (크기 고정)
const d = 4; // zoom 비율
const camera = new THREE.OrthographicCamera(-d * aspect, d * aspect, d, -d, 0.1, 1000);
camera.position.set(0, -3.0, 10);
camera.lookAt(0, 0, 0);

// 렌더러 생성 및 색상 보정
const renderer = new THREE.WebGLRenderer({ antialias: true });
renderer.setSize(width, height);
renderer.setPixelRatio(window.devicePixelRatio);
renderer.outputEncoding = THREE.sRGBEncoding;
container.appendChild(renderer.domElement);

// 밝은 조명 설정 (BMW 느낌)
const ambientLight = new THREE.AmbientLight(0xffffff, 1.0);
scene.add(ambientLight);

const keyLight = new THREE.DirectionalLight(0xfff2e5, 1.7);
keyLight.position.set(10, 10, 10);
scene.add(keyLight);

const rimLight = new THREE.DirectionalLight(0xe0e0ff, 0.8);
rimLight.position.set(-10, 5, -8);
scene.add(rimLight);


// spotlight 추가
const spotLight = new THREE.SpotLight(0xffffff, 1.2);
spotLight.position.set(5, 10, 5);
spotLight.target.position.set(0, 0, 0);
scene.add(spotLight, spotLight.target);


renderer.shadowMap.enabled = true;
renderer.shadowMap.type = THREE.PCFSoftShadowMap;
renderer.toneMapping = THREE.ACESFilmicToneMapping; // 고급 톤매핑
renderer.toneMappingExposure = 1.25;


const planeGeo = new THREE.PlaneGeometry(100, 100);
const planeMat = new THREE.MeshStandardMaterial({
    color: 0xf0f0f0,
    metalness: 0.3,
    roughness: 0.4
});
const plane = new THREE.Mesh(planeGeo, planeMat);
plane.rotation.x = -Math.PI / 2;
plane.position.y = -2;
plane.receiveShadow = true;
scene.add(plane);



// OrbitControls 설정
const controls = new OrbitControls(camera, renderer.domElement);
controls.enableDamping = true;
controls.dampingFactor = 0.1;
controls.enableZoom = false;
controls.minPolarAngle = Math.PI / 2;
controls.maxPolarAngle = Math.PI / 2;

// 현재 로딩된 차량 모델
let currentModel = null;
const token = 'Bearer ' + localStorage.getItem("token");

// 차량 로딩 함수
function loadCar(slotId) {
    alert(`슬롯 ${slotId} 차량 로딩 중...`);
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
                        // 기존 모델 삭제 (필수)
                        child.geometry.dispose();
                        if (child.material.map) child.material.map.dispose();
                        child.material.dispose();

                        child.material = new THREE.MeshStandardMaterial({
                            color: 0xffffff,
                            metalness: 0.9,
                            roughness: 0.15,
                            envMapIntensity: 1.5
                        });

                        // 그림자 설정 (효과적용 가능하면 사용)
                        child.castShadow = true;
                        child.receiveShadow = true;
                    }
                });
            }

            loader.parse(buffer, '', (gltf) => {
                currentModel = gltf.scene;

                const box = new THREE.Box3().setFromObject(currentModel);
                const center = box.getCenter(new THREE.Vector3());
                const size = box.getSize(new THREE.Vector3());

                // 차량 중앙 정렬 + 위치 조정
                currentModel.position.sub(center);
                currentModel.position.y -= size.y * 250.0;

                // 등장 애니메이션: 초기 scale 0.01로 시작
                const targetScale = new THREE.Vector3(350, 350, 350);
                currentModel.scale.set(0.01, 0.01, 0.01);  // 작게 시작
                scene.add(currentModel);

                // 회전 중심을 화면 하단 기준으로 보정
                controls.target.set(0, -size.y * 0.3, 0);
                controls.update();

                let progress = 0;
                const scaleUp = () => {
                    if (progress < 1) {
                        progress += 0.02;
                        currentModel.scale.lerp(targetScale, 0.1);
                        requestAnimationFrame(scaleUp);
                    } else {
                        currentModel.scale.copy(targetScale);  // 정확히 마무리
                    }
                };
                scaleUp();
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

// 반응형 처리
window.addEventListener('resize', () => {
    const width = container.clientWidth;
    const height = container.clientHeight;
    const aspect = width / height;
    camera.left = -d * aspect;
    camera.right = d * aspect;
    camera.top = d;
    camera.bottom = -d;
    camera.updateProjectionMatrix();
    renderer.setSize(width, height);
});

// 전역 등록
window.loadCar = loadCar;
loadCar(1);
