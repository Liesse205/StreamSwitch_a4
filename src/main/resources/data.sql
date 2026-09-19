-- =============================================
-- StreamSwitch Seed Data
-- Run automatically on application startup
-- =============================================

-- =====================
-- 1. TV Providers
-- =====================
INSERT INTO tv_providers (name, description, website_url, country)
VALUES
  ('DStv', 'MultiChoice satellite television service serving Africa with premium entertainment, sports, and news', 'https://www.dstv.com', 'Nigeria'),
  ('Canal+', 'French premium television channel offering movies, series, and live sports across Africa', 'https://www.canalplus.com', 'France'),
  ('StarTimes', 'Affordable digital TV provider offering pay-TV services across Africa and Asia', 'https://www.startimestv.com', 'China')
ON CONFLICT (name) DO NOTHING;

-- =====================
-- 2. Channels
-- =====================
INSERT INTO channels (name, channel_number, genre, description, logo_url)
VALUES
  ('SuperSport', '158', 'Sports', 'Premier African sports channel with live football, rugby, and cricket', 'https://example.com/supersport.png'),
  ('CNN', '401', 'News', '24-hour global news network covering world events and business', 'https://example.com/cnn.png'),
  ('BBC News', '402', 'News', 'British Broadcasting Corporation world news and current affairs', 'https://example.com/bbcnews.png'),
  ('Al Jazeera', '403', 'News', 'International news channel covering global politics and human interest stories', 'https://example.com/aljazeera.png'),
  ('Cartoon Network', '601', 'Kids', 'Animated cartoons and kids entertainment programming', 'https://example.com/cartoonnetwork.png'),
  ('Nickelodeon', '602', 'Kids', 'Childrens entertainment with animated and live-action shows', 'https://example.com/nickelodeon.png'),
  ('M-Net', '101', 'Entertainment', 'Premium entertainment channel with movies, series, and reality shows', 'https://example.com/mnet.png'),
  ('Showmax', '102', 'Entertainment', 'African original series, movies, and international content', 'https://example.com/showmax.png'),
  ('StarTimes Novela', '301', 'Entertainment', 'Popular telenovela and drama series channel', 'https://example.com/startimesnovela.png'),
  ('Channel O', '502', 'Music', 'African music videos and entertainment countdown shows', 'https://example.com/channelo.png')
ON CONFLICT (name) DO NOTHING;

-- =====================
-- 3. Subscription Packages
-- =====================
INSERT INTO subscription_packages (name, description, monthly_price, billing_cycle, active, provider_id)
VALUES
  ('DStv Isange', 'Entry-level package with local channels and basic entertainment', 1500.00, 'monthly', true, (SELECT id FROM tv_providers WHERE name = 'DStv')),
  ('DStv Access', 'Mid-tier package with sports, news, and entertainment channels', 4500.00, 'monthly', true, (SELECT id FROM tv_providers WHERE name = 'DStv')),
  ('Canal+ Access', 'Basic Canal+ package with movies and series', 3000.00, 'monthly', true, (SELECT id FROM tv_providers WHERE name = 'Canal+')),
  ('Canal+ Family', 'Family package with kids, entertainment, and movie channels', 6500.00, 'monthly', true, (SELECT id FROM tv_providers WHERE name = 'Canal+')),
  ('StarTimes Basic', 'Affordable basic package with local and entertainment channels', 900.00, 'monthly', true, (SELECT id FROM tv_providers WHERE name = 'StarTimes')),
  ('StarTimes Classic', 'Classic package with entertainment, kids, and news channels', 2200.00, 'monthly', true, (SELECT id FROM tv_providers WHERE name = 'StarTimes'));

-- =====================
-- 4. Package-Channel Relationships (Many-to-Many)
-- =====================

-- DStv Isange: local/basic content
INSERT INTO package_channels (package_id, channel_id)
SELECT p.id, c.id
FROM subscription_packages p, channels c
WHERE p.name = 'DStv Isange' AND c.name IN ('Al Jazeera', 'StarTimes Novela')
ON CONFLICT DO NOTHING;

-- DStv Access: sports, news, entertainment
INSERT INTO package_channels (package_id, channel_id)
SELECT p.id, c.id
FROM subscription_packages p, channels c
WHERE p.name = 'DStv Access' AND c.name IN ('SuperSport', 'CNN', 'BBC News', 'Cartoon Network', 'M-Net', 'Channel O')
ON CONFLICT DO NOTHING;

-- Canal+ Access: movies and news
INSERT INTO package_channels (package_id, channel_id)
SELECT p.id, c.id
FROM subscription_packages p, channels c
WHERE p.name = 'Canal+ Access' AND c.name IN ('CNN', 'Showmax')
ON CONFLICT DO NOTHING;

-- Canal+ Family: family, kids, entertainment
INSERT INTO package_channels (package_id, channel_id)
SELECT p.id, c.id
FROM subscription_packages p, channels c
WHERE p.name = 'Canal+ Family' AND c.name IN ('CNN', 'Cartoon Network', 'Nickelodeon', 'Showmax')
ON CONFLICT DO NOTHING;

-- StarTimes Basic: local and basic entertainment
INSERT INTO package_channels (package_id, channel_id)
SELECT p.id, c.id
FROM subscription_packages p, channels c
WHERE p.name = 'StarTimes Basic' AND c.name IN ('Al Jazeera', 'StarTimes Novela', 'Channel O')
ON CONFLICT DO NOTHING;

-- StarTimes Classic: entertainment, kids, news
INSERT INTO package_channels (package_id, channel_id)
SELECT p.id, c.id
FROM subscription_packages p, channels c
WHERE p.name = 'StarTimes Classic' AND c.name IN ('BBC News', 'Nickelodeon', 'StarTimes Novela', 'Channel O')
ON CONFLICT DO NOTHING;
