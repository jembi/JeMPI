library(fastLink)
library(magrittr)

dfX <- read.csv("../../JeMPI_TestData/Kenya/results/dataset-1-100000-02.csv", header = TRUE)
varnames <- c("given_name", "family_name", "gender", "dob", "national_id")
dfX_block <- dfX[varnames]
fl.out <- fastLink(dfA = dfX_block, dfB = dfX_block, varnames = varnames, estimate.only = TRUE, verbose = TRUE)
m <- fl.out$p.gamma.k.m
u <- fl.out$p.gamma.k.u
for (i in seq_along(varnames)) {
  sprintf("%-15s %10.7f %10.7f", varnames[i], m[[i]][2], u[[i]][2]) %>% cat(seq = "\n")
}
