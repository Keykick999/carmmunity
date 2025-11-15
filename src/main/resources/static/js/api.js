async function apiCall(endpoint, method = 'GET', data = null) {
    const token = localStorage.getItem('token');

    const headers = {
        'Content-Type': 'application/json',
    };

    // 토큰이 존재하면 Authorization 헤더를 추가합니다.
    if (token) {
        headers['Authorization'] = `Bearer ${token}`;
    }

    const config = {
        method: method,
        headers: headers,
    };

    if (data && (method === 'POST' || method === 'PUT')) {
        config.body = JSON.stringify(data);
    }

    try {
        const response = await fetch(endpoint, config);

        if (response.ok) {
            // 🚩 [수정] 204 No Content (내용 없음) 응답인지 먼저 확인
            if (response.status === 204) {
                return null; // 204 응답은 내용이 없으므로 null 반환
            }

            // 🚩 [수정] 200 OK 등 다른 성공 응답(POST 포함)은 JSON을 파싱하여 반환
            try {
                return await response.json();
            } catch (e) {
                // JSON 파싱에 실패하면 (응답 본문이 비어있는 200 OK 등) null 반환
                return null;
            }

        } else {
            const errorText = await response.text();
            throw new Error(errorText || `API 호출 실패: ${response.status}`);
        }
    } catch (error) {
        console.error('API 통신 오류:', error.message);
        // alert('오류 발생: ' + error.message); // 오류를 상위로 던지므로 alert 주석 처리
        throw error;
    }
}