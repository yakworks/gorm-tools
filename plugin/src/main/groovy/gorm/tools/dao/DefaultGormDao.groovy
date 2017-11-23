package gorm.tools.dao

import grails.gorm.transactions.Transactional

@Transactional
class DefaultGormDao implements GormDao {

	DaoEventInvoker daoEventInvoker


	@Override
	void fireEvent(DaoEventType eventType, Object... args) {
		daoEventInvoker.invokeEvent(eventType, this, args)
	}
}
