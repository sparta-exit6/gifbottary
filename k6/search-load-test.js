import http from 'k6/http';
import { check, sleep } from 'k6';

const baseUrl = __ENV.BASE_URL || 'http://localhost:8080';
const apiVersion = __ENV.API_VERSION || 'v1';
const keyword = __ENV.KEYWORD || '스타벅스';

export const options = {
    stages: [
        { duration: '30s', target: 10 },   // 30초 동안 10명까지 올림
        { duration: '1m', target: 30 },    // 1분 동안 30명까지 올림
        { duration: '1m', target: 50 },    // 1분 동안 50명까지 올림
        { duration: '30s', target: 0 },    // 마지막 30초 동안 종료
    ],
    thresholds: {
        http_req_failed: ['rate<0.01'],    // 실패율 1% 미만
        http_req_duration: ['p(95)<1000'], // p95 1초 미만
        checks: ['rate>0.99'],             // 체크 성공률 99% 이상
    },
};

export default function () {
    const url = `${baseUrl}/api/${apiVersion}/products?keyword=${encodeURIComponent(keyword)}&page=0&size=10`;

    const res = http.get(url);

    check(res, {
        'status is 200': (r) => r.status === 200,
        'success is true': (r) => {
            const body = JSON.parse(r.body);
            return body.success === true;
        },
    });

    sleep(1);
}