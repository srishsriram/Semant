/* Srish Sriram, Bradley Slocum, Jin Hur*/

package M3;

import java.util.*;

public class Semant implements Absyn.Visitor<EnumSet<Value.Flags>, Type> {
    private static void usage() {
        throw new Error("Usage: java M3.Semant <module>");
    }

    static Semant visitor = new Semant();
    private Semant() {}
    
    public static void main(String[] args) {
        if (args.length != 1) usage();
        java.io.File file = new java.io.File(args[0]);
        try {
            TypeCheck(file);
        } catch (Exception e) {
            error(e.getMessage());
            return;
        } catch (Error e) {
            error(e.getMessage());
            return;
        }
        System.err.flush();
        Scope.Print(Scope.Top());
        System.out.flush();
    }

    static java.io.File file;
    static Value.Module TypeCheck(java.io.File file)
            throws ParseException, java.io.IOException {
        Semant.file = file; 
        Parser parse = new Parser(new java.io.FileInputStream(file));
        Absyn.Decl.Module main = parse.Unit();
        String[] split = file.getName().split("\\.");
        String prefix, suffix;
        if (split.length == 2) {
            prefix = split[0];
            suffix = split[1];
        } else {
            prefix = file.getName();
            suffix = main.stmts == null ? "i3" : "m3";
        }
        if (!(prefix.equals(main.id()))
                || !(suffix.equals("m3") && main.stmts != null
                || !(suffix.equals("i3") && main.stmts == null)))
            warning(main, "file name (" + file.getName()
                    + ") doesn't match module name");
        Value.Module m = new Value.Module(main);
        if (m.isInterface)
            Value.Module.PushInterface(m.name);
        Value.TypeCheck(m);
        if (m.isInterface)
            Value.Module.PopInterface();
        Value.Module.NoteVisibility(m);
        return m;
    }

    static boolean anyErrors = false;

    private static void message(Absyn loc, String msg) {
        System.err.println(msg + ": line " + loc.line() + ", column "
                + loc.column());
    }

    static void error(Absyn node, String msg) {
        anyErrors = true;
        message(node, msg);
    }

    static void error(Absyn.Decl decl, String msg) {
        anyErrors = true;
        message(decl, msg + " (" + decl.id() + ")");
    }

    static void error(Absyn.Expr.Named named, String msg) {
        anyErrors = true;
        message(named, msg + "(" + named.id() + ")");
    }

    static void error(Type.Named named, String msg) {
        anyErrors = true;
        message(named.ast, msg + "(" + named.name + ")");
    }

    static void error(Type type, String msg) {
        anyErrors = true;
        message(type.ast, msg);
    }

    static void error(String msg) {
        anyErrors = true;
        System.err.println(msg);
    }

    static void warning(Absyn loc, String msg) {
        message(loc, "warning: " + msg);
    }

    static {
        Scope.Insert(new Value.Tipe("INTEGER", Type.INTEGER));
        Scope.Insert(new Value.Tipe("BOOLEAN", Type.BOOLEAN));
        Scope.Insert(Type.Enum.LookUp(Type.BOOLEAN, "FALSE"));
        Scope.Insert(Type.Enum.LookUp(Type.BOOLEAN, "TRUE"));
        Scope.Insert(new Value.Tipe("CHAR", Type.CHAR));
        Scope.Insert(new Value.Tipe("NULL", Type.NULL));
        Scope.Insert(new Value.Constant("NIL", 0, Type.NULL));
        Scope.Insert(new Value.Tipe("REFANY", Type.REFANY));
        Scope.Insert(new Value.Tipe("ROOT", Type.ROOT));
        Scope.Insert(new Value.Tipe("TEXT", Type.TEXT));

        Scope.Insert(new Value.Procedure("FIRST", Type.FIRST));
        Scope.Insert(new Value.Procedure("LAST", Type.LAST));
        Scope.Insert(new Value.Procedure("ORD", Type.ORD));
        Scope.Insert(new Value.Procedure("VAL", Type.VAL));
        Scope.Insert(new Value.Procedure("NUMBER", Type.NUMBER));
        Scope.Insert(new Value.Procedure("NEW", Type.NEW));
    }

    Absyn.Stmt currentLoop = null;

    /**
     * Map statement to its scope (Stmt.For, Stmt.Block).
     */
    static final Map<Absyn.Stmt, Scope> scopes = new HashMap<Absyn.Stmt, Scope>();

    @Override
    public Type visit(Absyn.Stmt.Assign s, EnumSet<Value.Flags> _) {
        // TODO
	
	Type tlhs = Expr.TypeCheck(s.lhs,_);
	Type trhs = Expr.TypeCheck(s.rhs,_);
		
	/*Stmt.AssignStmt.Check(tlhs, s.rhs);

	if (tlhs == Type.ERROR || trhs == Type.ERROR)
	    return Type.ERROR;
	
	if (!_.contains(Value.Flags.isDesignator)) {
	    throw new Error("left-hand side is not a designator");
	}
	if (!_.contains(Value.Flags.isWritable)) {
	    throw new Error("left-hand side is read-only");
	}
	
	return Stmt.TypeCheck(s,_);*/
	return null;
    }

    @Override
    public Type visit(Absyn.Stmt.Call s, EnumSet<Value.Flags> _) {
        // TODO
	Type type = Expr.TypeCheck(s.expr,_);
	
	//if (type == Type.ERROR)
	//    throw new Error("expression is not a statement");
        //return Stmt.TypeCheck(s,_);
	return type;
    }

    @Override
    public Type visit(Absyn.Stmt.Exit s, EnumSet<Value.Flags> _) {
        // TODO
	Type type = Stmt.TypeCheck(s);

	if (currentLoop == null) {
	    //throw new Error("EXIT not contained in a loop");
	    error(s, "EXIT not contained in a loop");
	}

	return type;
    }

    @Override
    public Type visit(Absyn.Stmt.Eval s, EnumSet<Value.Flags> _) {
        // TODO
	Type type = Expr.TypeCheck(s.expr,_);
	
	if (type == Type.ERROR || s.expr == null) 
	    //throw new Error("expression doesn't have a value");
	    error(s, "expression doesn't have a value");
        return type;
    }

    @Override
    public Type visit(Absyn.Stmt.For s, EnumSet<Value.Flags> _) {
        // TODO !!!

	Type from = Expr.TypeCheck(s.from,_);
	Type to = Expr.TypeCheck(s.to,_);
	Type by = Expr.TypeCheck(s.by,_);

	if (from == Type.ERROR || to == Type.ERROR)
	    return Type.ERROR;

	
	if(Type.Enum.Is(from) != null) {
	    if(!Type.IsEqual(from, to)) {
		//throw new Error("'from' and 'to' expressions are incompatible");
		error(s, "'from' and 'to' expressions are incompatible");
	    }
	}
	else if(from == Type.INTEGER || to == Type.INTEGER) {
	    Scope.PushNewOpen();
	    Scope.Insert(new Value.Variable(s, Type.INTEGER));
	    for (Absyn.Stmt stmt : s.stmts) {
		Stmt.TypeCheck(stmt);
	    }
	    Scope.PopNew();
	}
	else {
	    //throw new Error("'from' and 'to' expressions must be compatible ordinals");
	    error(s,"'from' and 'to' expressions must be compatible ordinals");
	}

	if(by != Type.INTEGER && s.by != null) {
	    //throw new Error("'by' expression must be an integer");
	    error(s,"'by' expression must be an integer");
	}

        return from;
    }

    @Override
    public Type visit(Absyn.Stmt.If s, EnumSet<Value.Flags> _) {
        // TODO

	for (Absyn.Stmt.If.Clause c : s.clauses) {
	    Stmt.TypeCheck(c);
	}

        return null;
    }

    @Override
    public Type visit(Absyn.Stmt.If.Clause c, EnumSet<Value.Flags> _) {
        // TODO
	
	Type type = Expr.TypeCheck(c.expr,_);
	if (c.expr != null && type != Type.BOOLEAN)
	    //throw new Error("IF condition must be a BOOLEAN");
	    error(c, "IF condition must be a BOOLEAN");
	return type;
    }

    @Override
    public Type visit(Absyn.Stmt.Loop s, EnumSet<Value.Flags> _) {
        // TODO

	for (Absyn.Stmt st : s.stmts)
	    Stmt.TypeCheck(st);	
	
	return null;

    }

    @Override
    public Type visit(Absyn.Stmt.Repeat s, EnumSet<Value.Flags> _) {
        // TODO !!!

	//	Type type = Stmt.TypeCheck(s);	
	
	Type exprType = Expr.TypeCheck(s.expr,_);
	if (exprType != Type.BOOLEAN)
	    //throw new Error("REPEAT condition must be a BOOLEAN");
	    error(s, "REPEAT condition must be a BOOLEAN");

        return exprType;
    }

    @Override
    public Type visit(Absyn.Stmt.Return s, EnumSet<Value.Flags> _) {
        // TODO !!!

	Type type = Expr.TypeCheck(s.expr,_);	
       
	if (s.expr == null)
	    //throw new Error("missing return result");
	    error(s, "missing return result");
	
        return type;
    }

    @Override
    public Type visit(Absyn.Stmt.While s, EnumSet<Value.Flags> _) {
        // TODO

	Type expr = Expr.TypeCheck(s.expr,_);

	for (Absyn.Stmt st : s.stmts)
	    Stmt.TypeCheck(st);

	if (expr == Type.ERROR)
	    return Type.ERROR;

	if (expr != Type.BOOLEAN)
	    //throw new Error("WHILE condition must be a BOOLEAN");
	    error(s, "WHILE condition must be a BOOLEAN");
	    
        return null;
    }

    @Override
	public Type visit(Absyn.Stmt.Block s, EnumSet<Value.Flags> _) {
        // TODO

	Scope.PushNewOpen();
	for (Absyn.Decl d : s.decls)
	    d.accept(this, _);
	for (Absyn.Stmt stmt : s.stmts)
	    stmt.accept(this, _);
	Scope.PopNew();

	return null;
    }

    @Override
    public Type visit(Absyn.Type.Array t, EnumSet<Value.Flags> _) {
        return new Type.OpenArray(t, t.type.accept(this, _));
    }

    @Override
    public Type visit(Absyn.Type.Named t, EnumSet<Value.Flags> _) {
        return new Type.Named(t);
    }

    @Override
    public Type visit(Absyn.Type.Object t, EnumSet<Value.Flags> _) {
        Type parent = t.parent == null ? Type.ROOT : t.parent.accept(this, _);
        Type.Object type = new Type.Object(t, parent);
        {
            Scope zz = Scope.Push(type.fields);
            for (Absyn.Decl.Field f : t.fields)
                Scope.Insert(new Value.Field(f, f.type.accept(this, _)));
            Scope.Pop(zz);
        }
        {
            Scope zz = Scope.Push(type.methods);
            for (Absyn.Decl.Method m : t.methods)
                Scope.Insert(new Value.Method(type, m, (Type.Proc) m.type.accept(this, _)));
            for (Absyn.Decl.Method m : t.overrides)
                Scope.Insert(new Value.Method(type, m, null));
            Scope.Pop(zz);
        }
        return type;
    }

    @Override
    public Type visit(Absyn.Type.Proc t, EnumSet<Value.Flags> _) {
        Type result = t.result == null ? null : t.result.accept(this, _);
        int numArgs = t.formals.size();
        Type.Proc type = new Type.Proc(t, numArgs, numArgs, result) {
            @Override
            Type check(Absyn.Expr.Call call) {
                Value.currentBody.isLeaf = false;
                Value.Formal.CheckArgs(call.actuals, Scope.ToList(formals), call.expr);
                if (result != null)
                    result = Type.Check(result);
                return result;
            }
        };
        Scope zz = Scope.Push(type.formals);
        for (Absyn.Decl.Formal f : t.formals)
            Scope.Insert(new Value.Formal(f, f.type.accept(this, _)));
        Scope.Pop(zz);
        return type;
    }

    @Override
    public Type visit(Absyn.Type.Ref t, EnumSet<Value.Flags> _) {
        return new Type.Ref(t, t.type.accept(this, _));
    }

    @Override
    public Type visit(Absyn.Decl.Field d, EnumSet<Value.Flags> _) {
        throw new Error("unreachable");
    }

    @Override
    public Type visit(Absyn.Decl.Formal d, EnumSet<Value.Flags> _) {
        throw new Error("unreachable");
    }

    @Override
    public Type visit(Absyn.Decl.Method d, EnumSet<Value.Flags> _) {
        throw new Error("unreachable");
    }

    @Override
    public Type visit(Absyn.Decl.Module d, EnumSet<Value.Flags> _) {
        Value.Module m = new Value.Module(d);
        Value.TypeCheck(m);
        return null;
    }

    @Override
    public Type visit(Absyn.Decl.Procedure d, EnumSet<Value.Flags> _) {
        Type.Proc type = (Type.Proc) d.type.accept(this, _);
        Scope.Insert(new Value.Procedure(d, type));
        return null;
    }

    @Override
    public Type visit(Absyn.Decl.Tipe d, EnumSet<Value.Flags> _) {
        Type t = d.type.accept(this, _);
        Scope.Insert(new Value.Tipe(d, t));
        return null;
    }

    @Override
    public Type visit(Absyn.Decl.Variable d, EnumSet<Value.Flags> _) {
        Type type = d.type == null ? null : d.type.accept(this, _);
        Scope.Insert(new Value.Variable(d, type));
        return null;
    }

    @Override
    public Type visit(Absyn.Expr.Or e, EnumSet<Value.Flags> flags) {
        // TODO
	Type lhs = Expr.TypeCheck(e.left,flags);
	Type rhs = Expr.TypeCheck(e.right,flags);

	if (!Type.IsEqual(lhs, rhs)) 
	    throw new Error("illegal operand(s)");

        return lhs;
    }

    @Override
    public Type visit(Absyn.Expr.And e, EnumSet<Value.Flags> flags) {
        // TODO

	Type lhs = Expr.TypeCheck(e.left,flags);
	Type rhs = Expr.TypeCheck(e.right,flags);

	if (!Type.IsEqual(lhs, rhs))
	    throw new Error("illegal operand(s)");

        return lhs;
    }

    @Override
    public Type visit(Absyn.Expr.Not e, EnumSet<Value.Flags> flags) {
        // TODO

	Type type = Expr.TypeCheck(e.expr,flags);

	if (type == Type.ERROR)
	    throw new Error("illegal operand(s)");

        return type;
    }

    @Override
    public Type visit(Absyn.Expr.Lt e, EnumSet<Value.Flags> flags) {
	// TODO

	Type t1 = Expr.TypeCheck(e.left,flags);
	Type t2 = Expr.TypeCheck(e.right,flags);

	if(t1==Type.ERROR || t2==Type.ERROR);
	else if(t1!=Type.INTEGER || t2!=Type.INTEGER)
	    throw new Error("illegal operand(s)");
	    
        return Type.BOOLEAN;
    }

    @Override
    public Type visit(Absyn.Expr.Gt e, EnumSet<Value.Flags> flags) {
        // TODO

	Type t1 = Expr.TypeCheck(e.left,flags);
        Type t2 = Expr.TypeCheck(e.right,flags);

        if(t1==Type.ERROR || t2==Type.ERROR);
        else if(t1!=Type.INTEGER || t2!=Type.INTEGER)
            throw new Error("illegal operand(s)");

        return Type.BOOLEAN;

    }

    @Override
    public Type visit(Absyn.Expr.Le e, EnumSet<Value.Flags> flags) {
        // TODO

	Type t1 = Expr.TypeCheck(e.left,flags);
        Type t2 = Expr.TypeCheck(e.right,flags);

        if(t1==Type.ERROR || t2==Type.ERROR);
        else if(t1!=Type.INTEGER || t2!=Type.INTEGER)
            throw new Error("illegal operand(s)");

        return Type.BOOLEAN;

    }
    @Override
    public Type visit(Absyn.Expr.Ge e, EnumSet<Value.Flags> flags) {
        // TODO
	Type t1 = Expr.TypeCheck(e.left,flags);
        Type t2 = Expr.TypeCheck(e.right,flags);

        if(t1==Type.ERROR || t2==Type.ERROR);
        else if(t1!=Type.INTEGER || t2!=Type.INTEGER)
            throw new Error("illegal operand(s)");

        return Type.BOOLEAN;
    }

    @Override
    public Type visit(Absyn.Expr.Eq e, EnumSet<Value.Flags> flags) {
        // TODO
	Type t1 = Expr.TypeCheck(e.left,flags);
        Type t2 = Expr.TypeCheck(e.right,flags);

	if (!Type.IsEqual(t1,t2));
        else if(t1!=Type.INTEGER || t2!=Type.INTEGER)
            throw new Error("illegal operand(s)");

        return Type.BOOLEAN;
    }

    @Override
    public Type visit(Absyn.Expr.Ne e, EnumSet<Value.Flags> flags) {
        // TODO
	Type t1 = Expr.TypeCheck(e.left,flags);
        Type t2 = Expr.TypeCheck(e.right,flags);

	if (t1==Type.ERROR || t2==Type.ERROR)
	    return Type.ERROR;
	else if(t1!=Type.INTEGER || t2!=Type.INTEGER)
            throw new Error("illegal operand(s)");
	
        return Type.BOOLEAN;
    }

    @Override
    public Type visit(Absyn.Expr.Add e, EnumSet<Value.Flags> flags) {
        // TODO

        Type lhs = Expr.TypeCheck(e.left,flags);
        Type rhs = Expr.TypeCheck(e.right,flags);

	if (Type.IsEqual(lhs, rhs))
	    return lhs;
	else if ( (Type.IsEqual(lhs, Type.INTEGER) && Type.IsEqual(rhs, Type.TEXT)) || (Type.IsEqual(lhs, Type.TEXT) && Type.IsEqual(rhs, Type.INTEGER)) )
	    return Type.TEXT;
	else if ( (Type.IsEqual(lhs, Type.INTEGER) && Type.IsEqual(rhs, Type.CHAR)) || (Type.IsEqual(lhs, Type.CHAR) && Type.IsEqual(rhs, Type.INTEGER)) )
	    return Type.CHAR;
	else if ( (Type.IsEqual(lhs, Type.CHAR) && Type.IsEqual(rhs, Type.TEXT)) || (Type.IsEqual(lhs, Type.TEXT) && Type.IsEqual(rhs, Type.CHAR)) )
	    return Type.TEXT;
	else 
	    throw new Error("illegal operand(s)");

    }

    @Override
    public Type visit(Absyn.Expr.Subtract e, EnumSet<Value.Flags> flags) {
        // TODO

        Type lhs = Expr.TypeCheck(e.left,flags);
        Type rhs = Expr.TypeCheck(e.right,flags);

        if (Type.IsEqual(lhs, rhs))
            return lhs;
        else if ( (Type.IsEqual(lhs, Type.INTEGER) && Type.IsEqual(rhs, Type.TEXT)) || (Type.IsEqual(lhs, Type.TEXT) && Type.IsEqual(rhs, Type.INTEGER)) )
            return Type.TEXT;
        else if ( (Type.IsEqual(lhs, Type.INTEGER) && Type.IsEqual(rhs, Type.CHAR)) || (Type.IsEqual(lhs, Type.CHAR) && Type.IsEqual(rhs, Type.INTEGER)) )
            return Type.CHAR;
        else if ( (Type.IsEqual(lhs, Type.CHAR) && Type.IsEqual(rhs, Type.TEXT)) || (Type.IsEqual(lhs, Type.TEXT) && Type.IsEqual(rhs, Type.CHAR)) )
            return Type.TEXT;
        else
            throw new Error("illegal operand(s)");

    }

    @Override
    public Type visit(Absyn.Expr.Multiply e, EnumSet<Value.Flags> flags) {
        // TODO
	Type lhs = Expr.TypeCheck(e.left,flags);
        Type rhs = Expr.TypeCheck(e.right,flags);

        if (Type.IsEqual(lhs, rhs))
            return lhs;
	else if ( (Type.IsEqual(lhs, Type.INTEGER) && Type.IsEqual(rhs, Type.TEXT)) || (Type.IsEqual(lhs, Type.TEXT) && Type.IsEqual(rhs, Type.INTEGER)) )
            return Type.TEXT;
        else if ( (Type.IsEqual(lhs, Type.INTEGER) && Type.IsEqual(rhs, Type.CHAR)) || (Type.IsEqual(lhs, Type.CHAR) && Type.IsEqual(rhs, Type.INTEGER)) )
            return Type.CHAR;
        else if ( (Type.IsEqual(lhs, Type.CHAR) && Type.IsEqual(rhs, Type.TEXT)) || (Type.IsEqual(lhs, Type.TEXT) && Type.IsEqual(rhs, Type.CHAR)) )
            return Type.TEXT;
        else
            throw new Error("illegal operand(s)");
    }

    @Override
    public Type visit(Absyn.Expr.Div e, EnumSet<Value.Flags> flags) {
        // TODO
        Type lhs = Expr.TypeCheck(e.left,flags);
        Type rhs = Expr.TypeCheck(e.right,flags);

        if (Type.IsEqual(lhs, rhs))
            return lhs;
        else if ( (Type.IsEqual(lhs, Type.INTEGER) && Type.IsEqual(rhs, Type.TEXT)) || (Type.IsEqual(lhs, Type.TEXT) && Type.IsEqual(rhs, Type.INTEGER)) )
            return Type.TEXT;
        else if ( (Type.IsEqual(lhs, Type.INTEGER) && Type.IsEqual(rhs, Type.CHAR)) || (Type.IsEqual(lhs, Type.CHAR) && Type.IsEqual(rhs, Type.INTEGER)) )
            return Type.CHAR;
        else if ( (Type.IsEqual(lhs, Type.CHAR) && Type.IsEqual(rhs, Type.TEXT)) || (Type.IsEqual(lhs, Type.TEXT) && Type.IsEqual(rhs, Type.CHAR)) )
            return Type.TEXT;
        else
            throw new Error("illegal operand(s)");
    }

    @Override
    public Type visit(Absyn.Expr.Mod e, EnumSet<Value.Flags> flags) {
        // TODO
        Type lhs = Expr.TypeCheck(e.left,flags);
        Type rhs = Expr.TypeCheck(e.right,flags);

        if (Type.IsEqual(lhs, rhs))
            return lhs;
        else if ( (Type.IsEqual(lhs, Type.INTEGER) && Type.IsEqual(rhs, Type.TEXT)) || (Type.IsEqual(lhs, Type.TEXT) && Type.IsEqual(rhs, Type.INTEGER)) )
            return Type.TEXT;
        else if ( (Type.IsEqual(lhs, Type.INTEGER) && Type.IsEqual(rhs, Type.CHAR)) || (Type.IsEqual(lhs, Type.CHAR) && Type.IsEqual(rhs, Type.INTEGER)) )
            return Type.CHAR;
        else if ( (Type.IsEqual(lhs, Type.CHAR) && Type.IsEqual(rhs, Type.TEXT)) || (Type.IsEqual(lhs, Type.TEXT) && Type.IsEqual(rhs, Type.CHAR)) )
            return Type.TEXT;
        else
            throw new Error("illegal operand(s)");
    }

    @Override
    public Type visit(Absyn.Expr.Plus e, EnumSet<Value.Flags> flags) {
        // TODO
	Type type = Expr.TypeCheck(e.expr, flags);
	if (type == Type.ERROR)
	    return Type.ERROR;
        return type;
    }

    @Override
    public Type visit(Absyn.Expr.Minus e, EnumSet<Value.Flags> flags) {
        // TODO
	Type type = Expr.TypeCheck(e.expr, flags);
	if (type == Type.ERROR)
	    return Type.ERROR;
        return type;
    }

    @Override
    public Type visit(Absyn.Expr.Deref e, EnumSet<Value.Flags> flags) {
        // TODO
        Type t=Expr.TypeCheck(e.expr,flags);

	if (t != Type.BOOLEAN) 

        //cannot dereference REFANY or NULL
        if (t==Type.REFANY||t==Type.NULL)
	    //throw new Error("cannot dereference REFANY or NULL");
	    error(e, "cannot dereference REFANY or NULL");
        //cannot dereference a non-REF value
        else if (Type.Ref.Is(t)==null)
	    //throw new Error("cannot dereference a non-REF value");
	    error(e, "cannot dereference a non-REF value");
        return t;
    }

    @Override
    public Type visit(Absyn.Expr.Qualify e, EnumSet<Value.Flags> flags) {
        return QualifyExpr.TypeCheck(e, flags);
    }

    @Override
    public Type visit(Absyn.Expr.Subscript e, EnumSet<Value.Flags> flags) {
        // TODO
	/*	
	Type t1=Expr.TypeCheck(e.expr,flags);
        Type t2=Expr.TypeCheck(e.index,flags);

        if (!Type.IsAssignable(t1,t2))
	    error(e,"illegal operand(s)");
        //subscripted expression is not an array
        else if(array = null)
	    error(e,"subscripted expression is not an array");
        //open arrays must be indexed by INTEGER expressions
        else if (array != null && t2==Type.ERROR)
	    error(e,"open arrays must be indexed by INTEGER expressions");
        else ;
	
        return null;
	*/

	
	Type type = Expr.TypeCheck(e, flags);
	Type.OpenArray array = Type.OpenArray.Is(type);
	if(type != Type.ERROR){
	    if(array == null)
		//throw new Error("subscripted expression is not an array");
		error(e, "subscripted expression is not an array");
	    Type indexType = Expr.TypeCheck(e.index);
	    if(indexType != Type.INTEGER)
		//throw new Error("open arrays must be indexed by INTEGER expressions");
		error(e, "open arrays must be indexed by INTEGER expressions");
	}
        return array;
	
    }

    @Override
    public Type visit(Absyn.Expr.Call s, EnumSet<Value.Flags> flags) {
        Type t = Expr.TypeCheck(s.expr);
        if (t == null)
            t = QualifyExpr.MethodType(s.expr);
        Type.Proc proc = Type.Proc.Is(t);
        if (proc != null) {
            proc.fixArgs(s);
            return proc.check(s);
        }
        if (t != Type.ERROR)
            error(s, "attempting to call a non-procedure");
        for (Absyn.Expr e : s.actuals)
            Expr.TypeCheck(e);
        return Type.ERROR;
    }

    @Override
    public Type visit(Absyn.Expr.Named e, EnumSet<Value.Flags> flags) {
        return NamedExpr.TypeCheck(e, flags);
    }

    @Override
    public Type visit(Absyn.Expr.Number e, EnumSet<Value.Flags> flags) {
        String[] split = e.token.image.split("_");
        if (split.length == 1) {
            try {
                Integer.parseInt(split[0]);
            } catch (NumberFormatException x) {
                error(e, "illegal INTEGER literal");
            }
        } else {
            assert split.length == 2;
            try {
                int radix = Integer.parseInt(split[0]);
                Integer.parseInt(split[1], radix);
            } catch (NumberFormatException x) {
                error(e, "illegal based INTEGER literal");
            }
        }
        return Type.INTEGER;
    }

    @Override
    public Type visit(Absyn.Expr.Char e, EnumSet<Value.Flags> flags) {
        return Type.CHAR;
    }

    @Override
    public Type visit(Absyn.Expr.Text e, EnumSet<Value.Flags> flags) {
        return Type.TEXT;
    }

    @Override
    public Type visit(Absyn.Expr.Type e, EnumSet<Value.Flags> flags) {
        TypeExpr.TypeCheck(e);
        return null;
    }

}
