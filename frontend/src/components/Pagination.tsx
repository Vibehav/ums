interface PaginationProps {
  page: number;
  totalPages: number;
  onPageChange: (page: number) => void;
}

export default function Pagination({ page, totalPages, onPageChange }: PaginationProps) {
  if (totalPages <= 1) return null;

  return (
    <div className="pagination">
      <button
        className="btn btn--secondary"
        disabled={page === 0}
        onClick={() => onPageChange(page - 1)}
      >
        Prev
      </button>

      {Array.from({ length: totalPages }, (_, i) => (
        <button
          key={i}
          className={`btn ${i === page ? 'btn--primary' : 'btn--secondary'}`}
          onClick={() => onPageChange(i)}
        >
          {i + 1}
        </button>
      ))}

      <button
        className="btn btn--secondary"
        disabled={page === totalPages - 1}
        onClick={() => onPageChange(page + 1)}
      >
        Next
      </button>
    </div>
  );
}
