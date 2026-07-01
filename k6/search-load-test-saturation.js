import http from 'k6/http';
import { check, sleep } from 'k6';

const baseUrl = __ENV.BASE_URL || 'http://localhost:8080';
const apiVersion = __ENV.API_VERSION || 'v2';
const keyword = __ENV.KEYWORD || '스타벅스';

export const options = {
    stages: [
        { duration: '30s', target: 30 },
        { duration: '1m', target: 80 },
        { duration: '1m', target: 120 },
        { duration: '1m', target: 150 },
        { duration: '30s', target: 0 },
    ],
    thresholds: {
        http_req_failed: ['rate<0.05'],
        http_req_duration: ['p(95)<2000'],
        checks: ['rate>0.95'],
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