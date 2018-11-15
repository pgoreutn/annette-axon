import annette.authorization.api.Permission
import annette.security.authorization.{CheckAll, Find}

val q1 = CheckAll(Permission("a"), Permission("b"))

val q2 = Find("p1", "p2")

val q3 = CheckAll(Permission("a"), Permission("b")) or Find("p1", "p2")

val q4 = CheckAll(Permission("a"), Permission("b")) and Find("p1", "p2")




//val q5 = CheckAll(Permission("a"), Permission("b")) or CheckAll(Permission("a"), Permission("b"))
//val q6 = CheckAll(Permission("a"), Permission("b")) and CheckAll(Permission("a"), Permission("b"))
//val q7 = CheckAll(Permission("a"), Permission("b")) or Find("p1", "p2") or Find("p1", "p2")
//val q8 = Find("p1", "p2") and Find("p1", "p2")
