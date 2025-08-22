export const DEFAULT_EXPO_IMAGE = import.meta.env.BASE_URL + "demo/keepout.jpg";
// base가 "/"면 결과는 "/demo/keepout.jpg"

// 수원 박람회 전용 포스터
export const SUWON_CAMP_POSTER =
  import.meta.env.BASE_URL + "demo/25gocaf_camping.png";

// (선택) 이 박람회의 실제 expoId를 알면 숫자로 넣어두면 더 정확하게 매칭됨.
// 모르면 그대로 null 두고, 제목 매칭만 사용해도 동작합니다.
export const SUWON_CAMP_POSTER_ID = 1;

export const SUWON_CAMP_TITLE = "2025 수원메쎄 고카프 더 파이널 시즌";
export const SUWON_CAMP_LOCATION = "수원메쎄 (수원역)";
