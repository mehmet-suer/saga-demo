UPDATE orders
SET status = 'DONE'
WHERE payment_state = 'COMPLETED'
  AND inventory_state = 'RESERVED'
  AND status <> 'DONE';
