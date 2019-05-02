#encoding "utf8"
#GRAMMAR_ROOT S

// Базовая грамматика - просто берем существительные
S -> Noun interp (Facts.Fact);

// TODO описать необходимые грамматики