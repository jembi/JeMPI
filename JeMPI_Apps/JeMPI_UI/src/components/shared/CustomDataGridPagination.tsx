import React, { forwardRef } from 'react';
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

export const CustomPagination = forwardRef<HTMLDivElement, MuiTablePaginationProps>((props, ref) => {
  const { slotProps, ...rest } = props;

  return (
    <GridPagination
      ActionsComponent={(subProps: any) => <Pagination {...subProps} />}
      ref={ref as any}
      slotProps={slotProps}
      {...rest} 
    />
  );
});

CustomPagination.displayName = 'CustomPagination';

export default CustomPagination;
