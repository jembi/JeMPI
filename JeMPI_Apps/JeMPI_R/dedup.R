library(fastLink)
library(magrittr)

# dfX <- read.csv("../devops/JeMPI_TestData/Kenya/results/test-data-0030000-02-10.csv", header = TRUE)
dfX <- read.csv("../../devops/JeMPI_TestData/Ethiopia/results/test-data-0049912-0025000-02-50.csv", header = TRUE)
# varnames <- c("given_name", "family_name", "gender", "dob", "city", "phone_number", "national_id")
# varnames <- c("phonetic_given_name", "phonetic_family_name", "gender", "dob", "nupi")
varnames <- c("given_name", "father", "fathers_father", "mother", "mothers_father", "gender", "dob", "phone_number")
dfX_block <- dfX[varnames]
fl.out <- fastLink(dfA = dfX_block, dfB = dfX_block, varnames = varnames, stringdist.method = 'jaro', estimate.only = TRUE, verbose = TRUE)
m <- fl.out$p.gamma.k.m
u <- fl.out$p.gamma.k.u
for (i in seq_along(varnames)) {
  sprintf("%-15s %10.7f %10.7f", varnames[i], m[[i]][2], u[[i]][2]) %>% cat(seq = "\n")
}








