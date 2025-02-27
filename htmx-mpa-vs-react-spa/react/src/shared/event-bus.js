/**
 * @typedef Subscriber
 * @param {?Object} data
 */


/**
 * @typedef Event 
 * @property {string} type
 * @property {Object | null} data
 */

/**
 * @typedef EventSubscriber
 * @property {string} event
 * @property {Subscriber} subscriber
 */

export class EventBus {
	/**
	 * @type {Map<string, Subscriber>}
	 */
	_subscribers = new Map();

	/**
	 * 
	 * @param {string} event 
	 * @param {Subscriber} subscriber 
	 * @returns {EventSubscriber}
	 */
	subscribe(event, subscriber) {
		let eventSubscribers = this._subscribers.get(event);
		if (!eventSubscribers) {
			eventSubscribers = [];
			this._subscribers.set(event, eventSubscribers);
		}

		eventSubscribers.push(subscriber);

		return { event, subscriber };
	}

	/**
	 * 
	 * @param {Event} event 
	 */
	publish(event) {
		const eventSubscribers = this._subscribers.get(event.type);
		if (eventSubscribers) {
			eventSubscribers.forEach(s => s(event.data));
		}
	}

	unsubscribe(subscriber) {
		const eventSubscribers = this._subscribers.get(subscriber.event);
		if (eventSubscribers) {
			const updatedEventSubscribers = eventSubscribers.filter(s => s != subscriber.subscriber);
			this._subscribers.set(subscriber.event, updatedEventSubscribers);
		}
	}

	clearAll() {
		this._subscribers.clear();
	}
}

export const eventBus = new EventBus();