package ca.freshstart.data.reconcile.types;

/**
 * ConcreteEventState.completed=none -> audited -> reconciled -> estimate
 */
public enum ConcreteEventReconcileState {
    none,
    audited, // аудит проведен, можно сделать согласование
    reconciled, // согласование проведено
    estimate // отправлено на эстимацию
}
