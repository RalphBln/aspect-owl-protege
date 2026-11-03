/**
 * OWL to unsorted TPTP FOL translation.
 *
 * <p>{@link xyz.aspectowl.tptp.renderer.OWL2TPTPObjectRenderer} uses unary class
 * predicates and binary object property predicates. The aspect renderers append
 * a context argument to each relational atom; equality retains its usual meaning.
 * Unqualified axioms and auxiliary class/property definitions hold in every context.
 * Qualified axioms hold at their aspect's context identifier.
 *
 * <p>Each aspect expression has a context identifier C_i. For an aspect A
 * qualified by B, a distinct composite context N is introduced, with the rule
 * N(X,C_n) implies A(X,C_a) and B(C_a,C_b). This construction is recursive;
 * siblings constrain the same base identifier and different nestings are distinct.
 *
 * <p>A single artificial predicate ctxBnd binds the object variables and all
 * context identifiers. Guarded rules require the context identifiers to belong
 * to their context predicates in the universal context CT, and an existential
 * guard instance prevents vacuous context bindings. The guard is never asserted
 * universally. These context-binding rules are triguarded; this does not make
 * arbitrary source OWL axioms (e.g. transitivity or number restrictions) triguarded.
 * Unsupported logical axiom types fail explicitly rather than being discarded.
 */
package xyz.aspectowl.tptp.renderer;
