import React from 'react';
import { GridPagination } from '@mui/x-data-grid';
import MuiPagination from '@mui/material/Pagination';
import { TablePaginationProps as MuiTablePaginationProps } from '@mui/material/TablePagination';

interface PaginationProps {
  count: number;
  page: number;
  rowsPerPage: number;
  onPageChange: (
    event: React.MouseEvent<HTMLElement> | React.ChangeEvent<unknown> | null,
    page: number
  ) => void;
  boundaryCount?: number;
  [key: string]: any;
}

const Pagination = ({
  count,
  page,
  rowsPerPage,
  onPageChange,
  boundaryCount = 2,
  ...other
}: PaginationProps) => {
  const pagesCount = Math.ceil(count / rowsPerPage);

  return (
    <MuiPagination
      count={pagesCount}
      page={page + 1}
      boundaryCount={boundaryCount}
      onChange={(event, page) => {
        onPageChange(event as React.ChangeEvent<unknown>, page - 1);
      }}
      {...other}
    />
  );
};

interface CustomPaginationProps extends Omit<MuiTablePaginationProps, 'component' | 'ref'> {
  [key: string]: any;
}

function CustomPagination(props: CustomPaginationProps) {
  return <GridPagination ActionsComponent={(subProps: any) => <Pagination {...subProps} />} {...props} />;
}

export default CustomPagination;
