import { ChevronLeft, ChevronRight } from "lucide-react";
import "../../../assets/css/reservation/Pagination.css";

function Pagination() {
  return (
    <div className="pagination-container">
      {/* Previous */}
      <button
        variant="ghost"
        className="pagination-nav-button"
      >
        <ChevronLeft className="chevron-icon" />
        <span className="nav-text">Previous</span>
      </button>

      {/* Page Numbers */}
      <div className="page-numbers">
        {/* Active Page */}
        <div className="page-active">
          <span className="page-text-active">1</span>
        </div>

        {/* Other Pages */}
        <button
          variant="ghost"
          className="page-button"
        >
          <span className="page-text">2</span>
        </button>

        <button
          variant="ghost"
          className="page-button page-button-hidden"
        >
          <span className="page-text">3</span>
        </button>

        <button
          variant="ghost"
          className="page-button page-button-hidden"
        >
          <span className="page-text">4</span>
        </button>
      </div>

      {/* Next */}
      <button
        variant="ghost"
        className="pagination-nav-button"
      >
        <span className="nav-text">Next</span>
        <ChevronRight className="chevron-icon" />
      </button>
    </div>
  );
}
export default Pagination;