#library(randomNames)
#library(stringdist)
library(fastLink)
#library(RecordLinkage)
library(magrittr)

dfX <- read.csv("../../JeMPI_TestData/Zambia/results/dataset-1-002000-05.csv", header = TRUE)
varnames <- c("given_name", "family_name", "gender", "dob", "city", "phone_number", "national_id")
dfX_block <- dfX[varnames]
fl.out <- fastLink(dfA = dfX_block, dfB = dfX_block, varnames = varnames, n.cores = 16, verbose = TRUE)
m <- fl.out$EM$p.gamma.k.m
u <- fl.out$EM$p.gamma.k.u
for (i in 1:length(varnames)) {
  sprintf("%-15s %10.7f %10.7f", varnames[i], m[[i]][2], u[[i]][2]) %>% cat(seq = "\n")
}
