-- Create custom indexes only needed for 1st Franklin

-- Improves the performance of the query that finds the user_id and company_id
-- by querying auth_user_profile where attrname = 'fffcCustomerId' and attrvalue
-- is the 1st Franklin Customer ID.
create index concurrently if not exists
	auth_user_profile_value
on
	auth_user_profile(attrvalue, attrname)
tablespace
	@DSS_DB_NAME@_idx;