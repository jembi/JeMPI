library(data.table)

githubURL <- "https://github.com/mjfii/R-NameParser-Lib/raw/master/data/census.names.RData"
load(url(githubURL))
female_names  <- as.data.frame((census.names[census.names$female_value > 0])[, c("name", "female_value")])
male_names    <- as.data.frame((census.names[census.names$male_value > 0])[, c("name", "male_value")])
last_names    <- as.data.frame((census.names[census.names$last_name_value > 0 & census.names$name != "ALL OTHER NAMES"])[, c("name", "last_name_value")])
female_names2 <- female_names[order(female_names$female_value, decreasing = TRUE), ]
male_names2 <- male_names[order(male_names$male_value, decreasing = TRUE), ]
last_names2   <- last_names[order(last_names$last_name_value, decreasing = TRUE),]

write.table(x = female_names2, file = "females.csv",    quote = FALSE, col.names = FALSE, sep = ",", row.names = FALSE)
write.table(x = male_names2,   file = "males.csv",      quote = FALSE, col.names = FALSE, sep = ",", row.names = FALSE)
write.table(x = last_names2,   file = "last_names.csv", quote = FALSE, col.names = FALSE, sep = ",", row.names = FALSE)
