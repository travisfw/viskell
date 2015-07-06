package nl.utwente.group10.haskell.hindley;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import nl.utwente.group10.haskell.exceptions.HaskellTypeError;
import nl.utwente.group10.haskell.expr.Expr;
import nl.utwente.group10.haskell.type.ConstT;
import nl.utwente.group10.haskell.type.Type;
import nl.utwente.group10.haskell.type.TypeClass;
import nl.utwente.group10.haskell.type.VarT;

/**
 * Implementation of the Hindley-Milner type system for Haskell types and expressions.
 */
public final class HindleyMilner {
    /**
     * Logger for this class.
     */
    private static final Logger logger = Logger.getLogger(HindleyMilner.class.getName());

    /**
     * Offset for the creation of variable types.
     */
    static int tvOffset = 0;

    static {
        HindleyMilner.logger.setLevel(Level.WARNING);
        // Changing this to Level.INFO will show debug messages.
    }
    
    /**
     * Private constructor - methods in this class are static.
     */
    private HindleyMilner() {
    }

    public static void unify(final Type t1, final Type t2) throws HaskellTypeError {
        unify(null, t1, t2);
    }

    public static void unify(final Expr context, final Type t1, final Type t2) throws HaskellTypeError {
        final Type a = t1.prune();
        final Type b = t2.prune();
        
        HindleyMilner.logger.info(String.format("Unifying types %s and %s for context %s", t1, t2, context));

        if (a instanceof VarT && !a.equals(b)) {
            // First, prevent ourselves from going into an infinite loop
            if (HindleyMilner.occursInType(a, b)) {
                HindleyMilner.logger.info(String.format("Recursion in types %s and %s for context %s", a, b, context));
                throw new HaskellTypeError(String.format("%s ∈ %s", a, b), context, a, b);
            }

            if (b instanceof VarT) {
                // Example: we have to unify (for example) α and β

                VarT va = ((VarT) a);
                VarT vb = ((VarT) b);

                if (va.hasConstraints() && vb.hasConstraints()) {
                    // Both a and b have constraints (type classes). Whatever type both of those are going to be must
                    // therefore lie inside the union of those type classes: if a must be in Num and Show, and b must be
                    // in Num and Read, then whatever the resulting type will be must be in Num, Show and Read.

                    VarT t = HindleyMilner.makeVariable(VarT.union(va, vb));

                    va.setInstance(t);
                    vb.setInstance(t);
                } else if (vb.hasConstraints()) {
                    // If a doesn't have constraints but b does, just use b's.
                    va.setInstance(vb);
                } else if (va.hasConstraints()) {
                    // If b doesn't have constraints but a does, just use a's.
                    vb.setInstance(va);
                } else {
                    // Neither a nor b has constraints, set a's instance to b.
                    va.setInstance(vb);
                }
            } else {
                // Example: we have to unify (for example) α and Int.
                // Do so by stating that α must be Int, provided Int fits in α's typeclasses
                if (((VarT) a).hasConstraint(b)) {
                    ((VarT) a).setInstance(b);
                } else {
                    HindleyMilner.logger.info(String.format("Unable to unify types %s and %s for context %s", a, b, context));
                    throw new HaskellTypeError(String.format("%s ∉ constraints of %s", b, a), context, a, b);
                }
            }
        } else if (a instanceof ConstT && b instanceof VarT) {
            // Example: we have to unify Int and α.
            // Same as above, but mirrored.
            HindleyMilner.unify(context, b, a);
        } else if (a instanceof ConstT && b instanceof ConstT) {
            // Example: we have to unify Int and Int.

            final ConstT ao = (ConstT) a;
            final ConstT bo = (ConstT) b;

            // If the constructor doesn't match, give up right away.
            // Example: trying to unify String and Int.
            if (!ao.getConstructor().equals(bo.getConstructor())) {
                HindleyMilner.logger.info(String.format("Unable to unify types %s and %s for context %s", a, b, context));
                throw new HaskellTypeError(String.format("%s ⊥ %s", a, b), context, a, b);
            }

            // If the two types have different amounts of arguments, bail.
            // Example: trying to unify (,) Int Int and (,) Int Int Int
            if (ao.getArgs().length != bo.getArgs().length) {
                HindleyMilner.logger.info(String.format("Unable to unify types %s and %s for context %s", a, b, context));
                throw new HaskellTypeError(String.format("%s ⊥ %s", a, b), context, a, b);
            }

            // Other than that, types can be unified if each of the arguments can be.
            for (int i = 0; i < ao.getArgs().length; i++) {
                HindleyMilner.unify(context, ao.getArgs()[i], bo.getArgs()[i]);
            }
        }
    }

    /**
     * Checks whether a given type exists within another type. This method is used to prevent loops.
     * @param a The first type.
     * @param b The second type.
     * @return Whether the first type occurs within the second type.
     */
    public static boolean occursInType(final Type a, final Type b) {
        final Type pruned = b.prune();
        boolean occurs = false;

        if (pruned.equals(a)) {
            occurs = true;
        } else if (pruned instanceof ConstT) {
            for (Type t : ((ConstT) pruned).getArgs()) {
                if (occursInType(a, t)) {
                    occurs = true;
                    break;
                }
            }
        }

        return occurs;
    }

    /**
     * Creates and returns a new {@code VarT} instance with a unique identifier.
     * @param constraints Constraints for the new VarT.
     * @return A new variable type.
     */
    public static VarT makeVariable(final Set<TypeClass> constraints) {
        final String name;

        name = HindleyMilner.tvOffset <= 25
                ? String.valueOf((char) ('α' + HindleyMilner.tvOffset))
                : Integer.toString(HindleyMilner.tvOffset);

        HindleyMilner.tvOffset += 1;

        return new VarT(name, constraints, null);
    }

    /**
     * Creates and returns a new {@code VarT} instance with a unique identifier.
     * @return A new variable type.
     */
    public static VarT makeVariable() {
        return makeVariable(new HashSet<TypeClass>());
    }
}
