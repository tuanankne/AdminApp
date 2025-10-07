-- Tạo các storage bucket cần thiết cho ứng dụng Admin

-- 1. Bucket cho service type icons (loại dịch vụ)
INSERT INTO storage.buckets (id, name, public, file_size_limit, allowed_mime_types)
VALUES (
    'servicetype',
    'servicetype',
    true,
    52428800, -- 50MB
    ARRAY['image/jpeg', 'image/png', 'image/webp', 'image/gif']
) ON CONFLICT (id) DO NOTHING;

-- 2. Bucket cho report images (báo cáo)
INSERT INTO storage.buckets (id, name, public, file_size_limit, allowed_mime_types)
VALUES (
    'report-images',
    'report-images',
    true,
    52428800, -- 50MB
    ARRAY['image/jpeg', 'image/png', 'image/webp', 'image/gif']
) ON CONFLICT (id) DO NOTHING;

-- 4. Bucket cho user avatars
INSERT INTO storage.buckets (id, name, public, file_size_limit, allowed_mime_types)
VALUES (
    'user-avatars',
    'user-avatars',
    true,
    10485760, -- 10MB
    ARRAY['image/jpeg', 'image/png', 'image/webp', 'image/gif']
) ON CONFLICT (id) DO NOTHING;

-- Tạo policies cho các bucket
-- Service Type bucket policies
CREATE POLICY "Public Access for servicetype" ON storage.objects
FOR SELECT USING (bucket_id = 'servicetype');

CREATE POLICY "Authenticated users can upload servicetype" ON storage.objects
FOR INSERT WITH CHECK (bucket_id = 'servicetype' AND auth.role() = 'authenticated');

CREATE POLICY "Authenticated users can update servicetype" ON storage.objects
FOR UPDATE USING (bucket_id = 'servicetype' AND auth.role() = 'authenticated');

CREATE POLICY "Authenticated users can delete servicetype" ON storage.objects
FOR DELETE USING (bucket_id = 'servicetype' AND auth.role() = 'authenticated');

-- Report Images bucket policies
CREATE POLICY "Public Access for report-images" ON storage.objects
FOR SELECT USING (bucket_id = 'report-images');

CREATE POLICY "Authenticated users can upload report-images" ON storage.objects
FOR INSERT WITH CHECK (bucket_id = 'report-images' AND auth.role() = 'authenticated');

CREATE POLICY "Authenticated users can update report-images" ON storage.objects
FOR UPDATE USING (bucket_id = 'report-images' AND auth.role() = 'authenticated');

CREATE POLICY "Authenticated users can delete report-images" ON storage.objects
FOR DELETE USING (bucket_id = 'report-images' AND auth.role() = 'authenticated');

-- User Avatars bucket policies
CREATE POLICY "Public Access for user-avatars" ON storage.objects
FOR SELECT USING (bucket_id = 'user-avatars');

CREATE POLICY "Authenticated users can upload user-avatars" ON storage.objects
FOR INSERT WITH CHECK (bucket_id = 'user-avatars' AND auth.role() = 'authenticated');

CREATE POLICY "Authenticated users can update user-avatars" ON storage.objects
FOR UPDATE USING (bucket_id = 'user-avatars' AND auth.role() = 'authenticated');

CREATE POLICY "Authenticated users can delete user-avatars" ON storage.objects
FOR DELETE USING (bucket_id = 'user-avatars' AND auth.role() = 'authenticated');
