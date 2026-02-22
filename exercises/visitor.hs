data Operator = Plus 
              | Minus
              | Star
              | Slash
              deriving (Show, Eq)

data Val = VD Double | VS String | VB Bool deriving (Eq)
instance Show Val where 
    show (VD d) = show d
    show (VS s) = show s
    show (VB b) = show b

data Expr = Binary Expr Operator Expr
          | Grouping Expr
          | Literal Val
          deriving (Show, Eq)

data ExprVisitor r = ExprVisitor
    { visitLiteral  :: Val                -> r
    , visitGrouping :: r                  -> r
    , visitBinary   :: r -> Operator -> r -> r
    }

foldExpr :: ExprVisitor r -> Expr -> r
foldExpr v (Literal l) = visitLiteral v l
foldExpr v (Grouping g) = visitGrouping v (foldExpr v g)
foldExpr v (Binary lhs op rhs) = visitBinary v (foldExpr v lhs) op (foldExpr v rhs)

prettyVisitor :: ExprVisitor String
prettyVisitor = ExprVisitor
    { visitLiteral  = show
    , visitGrouping = \s -> "(" ++ s ++ ")"
    , visitBinary   = \l op r -> l ++ " " ++ show op ++ " " ++ r
    }

main :: IO()
main = putStrLn $ foldExpr prettyVisitor (Binary (Literal $ VD 1.0) Plus (Literal $ VD 1.0))
