-- FAQ 초기 데이터 (PostgreSQL)
-- 목적: DB에 새로 FAQ 데이터를 넣을 때 운영 DB와 동일하게 맞추기 위한 sql 파일

-- [실행 방법]
-- 1. psql -h <host> -p <port> -U <user> -d <db> -f db/seed/faq.sql (데이터 넣을 해당 DB 정보값 입력)
-- 2. db 콘솔에 복사 붙여넣기 (BEGIN; ~ COMMIT; 까지)

-- [주의]
-- 1. Flyway처럼 자동 실행되지 않으므로 직접 실행해야 합니다.
-- 2. 이미 데이터가 있으면 아래 가드가 실행을 중단시킵니다.


\set ON_ERROR_STOP on

BEGIN;

SET client_encoding = 'UTF8';

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM faq) THEN
        RAISE EXCEPTION 'faq에 이미 데이터가 있습니다. 빈 테이블에서만 실행하세요.';
    END IF;
END
$$;
INSERT INTO faq (id, created_at, updated_at, active, answer, category, display_order, question) VALUES (1, now(), now(), true, '아필굿은 설문 조사 결과와 개인의 건강 정보, 섭취 이력 등을 종합적으로 분석해 사용자에게 가장 적합한 영양 성분을 추천해 드려요. 추천 결과는 참고용이며, 필요 시 전문가와 상담을 권장드려요!', 'RECOMMENDATION_INGREDIENT', 1, '영양 성분 추천은 어떻게 이루어지나요?');
INSERT INTO faq (id, created_at, updated_at, active, answer, category, display_order, question) VALUES (2, now(), now(), true, '연속 섭취일은 복구할 수 없어요.', 'INTAKE', 2, '연속 섭취일 복구 방법은 없나요?');
INSERT INTO faq (id, created_at, updated_at, active, answer, category, display_order, question) VALUES (3, now(), now(), true, '마이페이지 > 설정 > 복용 시간 알림에서 설정할 수 있어요.', 'NOTIFICATION', 3, '복용 알림은 어떻게 설정하나요?');
INSERT INTO faq (id, created_at, updated_at, active, answer, category, display_order, question) VALUES (5, now(), now(), true, '초기 설문에서 입력하신 나이, 성별, 건강 고민, 복용 중인 약물·알레르기 정보를 기반으로 아필굿의 자체 성분 DB(식약처 데이터 기반)와 AI 분석을 결합해 추천해 드려요. 광고성 문구가 아닌 실제 효능 근거를 기준으로 매칭됩니다.', 'RECOMMENDATION_INGREDIENT', 4, '영양제 추천은 어떤 기준으로 이루어지나요?');
INSERT INTO faq (id, created_at, updated_at, active, answer, category, display_order, question) VALUES (6, now(), now(), true, '섭취 중인 영양제를 등록하시면, 병용 시 주의가 필요한 성분 조합을 자동으로 필터링해 안내해 드려요. 다만 이는 참고용 정보이며, 정확한 복용 판단은 반드시 의사·약사와 상담해 주세요.', 'RECOMMENDATION_INGREDIENT', 5, '제가 먹고 있는 다른 영양제와 함께 먹어도 안전한가요?');
INSERT INTO faq (id, created_at, updated_at, active, answer, category, display_order, question) VALUES (7, now(), now(), true, '네, 마이페이지 > 설문 수정 혹은 홈페이지 > 정기 추천 리스트의 [설문 수정하기]를 통해 건강 고민이나 복용 정보를 언제든 다시 입력하실 수 있고, 수정 즉시 추천 결과에 반영돼요.', 'RECOMMENDATION_INGREDIENT', 6, '추천 결과가 마음에 안 들면 다시 받을 수 있나요?');
INSERT INTO faq (id, created_at, updated_at, active, answer, category, display_order, question) VALUES (8, now(), now(), true, '식품안전나라, 식약처 등 공신력 있는 공식 데이터베이스를 기반으로 아필굿이 직접 구축한 자체 DB예요. 과대광고성 문구는 제외하고 검증된 효능 정보만 제공하고 있어요.', 'RECOMMENDATION_INGREDIENT', 7, '영양 정보의 효능·주의사항은 어디서 가져온 정보인가요?');
INSERT INTO faq (id, created_at, updated_at, active, answer, category, display_order, question) VALUES (9, now(), now(), true, '홈 화면에서 오늘 섭취할 영양제 목록을 확인하고, 섭취 완료 시 체크만 하면 자동으로 기록돼요. 별도의 수기 입력은 필요 없어요.', 'INTAKE', 8, '복용 기록은 어떻게 남기나요?');
INSERT INTO faq (id, created_at, updated_at, active, answer, category, display_order, question) VALUES (10, now(), now(), true, '복용을 놓치면 연속 섭취 기록(스트릭)이 초기화되고, 캐릭터 성장 단계도 처음(씨앗 단계)으로 되돌아가요. 다만 언제든 다시 시작해서 새롭게 성장시켜 나가실 수 있어요.', 'INTAKE', 9, '복용을 깜빡하면 어떻게 되나요?');
INSERT INTO faq (id, created_at, updated_at, active, answer, category, display_order, question) VALUES (11, now(), now(), true, '네, 마이 캐비닛에 여러 제품을 등록해 원하는 시간대별로 함께 관리하실 수 있어요.', 'INTAKE', 10, '여러 영양제를 한 번에 등록해서 관리할 수 있나요?');
INSERT INTO faq (id, created_at, updated_at, active, answer, category, display_order, question) VALUES (12, now(), now(), true, '네, 마이 캐비닛에서 영양제 등록 시 매일 복용이 아닌 원하는 복용 주기를 선택하실 수 있어요. 설정한 주기 기준으로 연속 섭취일이 계산돼요.', 'INTAKE', 11, '매일 먹지 않고 주기적으로 먹는 영양제도 등록할 수 있나요?');
INSERT INTO faq (id, created_at, updated_at, active, answer, category, display_order, question) VALUES (13, now(), now(), true, '네, 마이 캐비닛에서 제품별로 원하는 복용 시간대를 자유롭게 설정하실 수 있고, 설정한 시간에 맞춰 푸시 알림을 보내드려요.', 'NOTIFICATION', 12, '복용 알림 시간을 직접 설정할 수 있나요?');
INSERT INTO faq (id, created_at, updated_at, active, answer, category, display_order, question) VALUES (14, now(), now(), true, '휴대폰 설정에서 아필굿 앱의 알림 권한이 꺼져 있지 않은지 먼저 확인해 주세요. 권한이 켜져 있는데도 알림이 오지 않으면 고객센터로 문의해 주세요.', 'NOTIFICATION', 13, '알림이 오지 않는데 어떻게 해야 하나요?');
INSERT INTO faq (id, created_at, updated_at, active, answer, category, display_order, question) VALUES (15, now(), now(), true, '마이페이지 > 알림 설정에서 전체 알림을 끄거나, 원하는 항목만 선택적으로 끌 수 있어요.', 'NOTIFICATION', 14, '알림을 아예 받고 싶지 않으면 어떻게 하나요?');
INSERT INTO faq (id, created_at, updated_at, active, answer, category, display_order, question) VALUES (16, now(), now(), true, '네, 복용 시간이 지나도록 체크하지 않으면 스트릭 유지를 위한 리마인드 알림을 보내드려요.', 'NOTIFICATION', 15, '스트릭이 끊기기 전에 미리 알려주나요?');
INSERT INTO faq (id, created_at, updated_at, active, answer, category, display_order, question) VALUES (17, now(), now(), true, '마이페이지 > 계정 설정 > 회원 탈퇴에서 진행하실 수 있어요. 탈퇴 시 저장된 설문, 복용 기록 등의 데이터는 모두 삭제되며 복구되지 않아요.', 'ETC', 16, '회원 탈퇴는 어떻게 하나요?');
INSERT INTO faq (id, created_at, updated_at, active, answer, category, display_order, question) VALUES (18, now(), now(), true, '네, 아필굿은 개인 비밀번호를 별도로 저장하지 않고 카카오·네이버의 공식 로그인 인증만 사용하기 때문에 안전하게 이용하실 수 있어요.', 'ETC', 17, '카카오·네이버 계정 로그인은 안전한가요?');
INSERT INTO faq (id, created_at, updated_at, active, answer, category, display_order, question) VALUES (19, now(), now(), true, '네, 건강 정보는 개인정보 보호 정책에 따라 암호화하여 안전하게 보관하며, 서비스 제공 목적 외에는 사용하지 않아요.', 'ETC', 18, '입력한 건강 정보는 안전하게 보관되나요?');
INSERT INTO faq (id, created_at, updated_at, active, answer, category, display_order, question) VALUES (20, now(), now(), true, '마이페이지 > 고객센터에서 1:1 문의를 남겨주시면 확인 후 빠르게 답변드릴게요.', 'ETC', 19, '문의사항은 어디로 하면 되나요?');
INSERT INTO faq (id, created_at, updated_at, active, answer, category, display_order, question) VALUES (21, now(), now(), true, '아필굿의 추천은 식약처 공식 데이터를 기반으로 한 참고용 정보 제공 서비스이며, 의학적 진단이나 처방을 대체하지 않아요. 기저질환이 있거나 다른 약물을 복용 중이신 경우 반드시 섭취 전 의사·약사와 상담해 주시기 바라며, 최종 섭취 결정에 대한 책임은 이용자 본인에게 있습니다.', 'ETC', 20, '아필굿의 추천대로 섭취했는데 문제가 생기면 책임은 누구에게 있나요?');


--
-- Name: faq_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('faq_id_seq', 21, true);

COMMIT;
