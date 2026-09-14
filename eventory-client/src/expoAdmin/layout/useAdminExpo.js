import { useOutletContext } from "react-router-dom";

/**
 * AdminLayout 이 하위 페이지에 내려주는 선택 박람회 정보
 * { expoId, expo, expos, selectExpo(id), reloadExpos() }
 */
export const useAdminExpo = () => useOutletContext();
