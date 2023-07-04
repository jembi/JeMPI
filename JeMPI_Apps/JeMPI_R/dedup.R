library(fastLink)
library(magrittr)

dfX <- read.csv("../../JeMPI_TestData/Reference/results/test-data-0005000-02.csv", header = TRUE)
varnames <- c("given_name", "family_name", "gender", "dob", "city", "phone_number", "national_id")
dfX_block <- dfX[varnames]
fl.out <- fastLink(dfA = dfX_block, dfB = dfX_block, varnames = varnames, stringdist.method = 'jaro', estimate.only = TRUE, verbose = TRUE)
m <- fl.out$p.gamma.k.m
u <- fl.out$p.gamma.k.u
for (i in seq_along(varnames)) {
  sprintf("%-15s %10.7f %10.7f", varnames[i], m[[i]][2], u[[i]][2]) %>% cat(seq = "\n")
}








