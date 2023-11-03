import { GridPagination } from '@mui/x-data-grid'
import MuiPagination from '@mui/material/Pagination'

const Pagination = ({
  count,
  page,
  rowsPerPage,
  onPageChange,
  boundaryCount,
  ...other
}: any) => {
  const pagesCount = Math.ceil(count / rowsPerPage)

  return (
    <MuiPagination
      count={pagesCount}
      page={page + 1}
      boundaryCount={boundaryCount}
      onChange={(event, page) => {
        onPageChange(event, page - 1)
      }}
      {...other}
    />
  )
}

function CustomPagination(props: any) {
  return <GridPagination ActionsComponent={Pagination} {...props} />
}

export default CustomPagination
